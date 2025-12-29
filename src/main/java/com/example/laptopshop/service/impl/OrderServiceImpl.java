package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartService cartService;
    private final CityRepository cityRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final CartDetailRepository cartDetailRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final WarrantyService warrantyService;

    @Override
    @Transactional
    public Order placeOrder(User user, String note, String shippingAddress,
                            String shippingName, String shippingPhone, Long cityId, Long paymentMethodId,
                            List<Long> selectedCartDetailIds, String voucherCode) {

        // 1. Lấy sản phẩm từ giỏ hàng
        List<CartDetail> selectedItems = cartDetailRepository.findAllById(selectedCartDetailIds);

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("Không có sản phẩm nào được chọn để thanh toán!");
        }

        // 2. Tính tổng tiền hàng
        BigDecimal totalProductsPrice = BigDecimal.ZERO;
        for (CartDetail item : selectedItems) {
            BigDecimal linePrice = item.getProduct().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalProductsPrice = totalProductsPrice.add(linePrice);
        }

        // 3. Tính phí vận chuyển
        City city = cityRepository.findById(cityId).orElseThrow(() -> new RuntimeException("City not found"));
        Long regionId = city.getRegion().getRegionId();

        ShippingRate rate = shippingRateRepository.findByRegionRegionId(regionId).orElse(null);
        BigDecimal shippingFee = (rate != null) ? rate.getBaseFee() : BigDecimal.valueOf(50000);

        // 4. Xử lý Voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucher = null;

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            voucher = voucherRepository.findByCode(voucherCode).orElse(null);

            if (voucher != null && "active".equals(voucher.getStatus())
                    && voucher.getQuantity() > 0
                    && LocalDateTime.now().isAfter(voucher.getStartDate())
                    && LocalDateTime.now().isBefore(voucher.getEndDate())) {

                // Kiểm tra đơn tối thiểu
                if (totalProductsPrice.compareTo(voucher.getMinOrderValue()) >= 0) {
                    BigDecimal percent = voucher.getDiscountPercent().divide(BigDecimal.valueOf(100));
                    discountAmount = totalProductsPrice.multiply(percent);

                    // Giới hạn giảm tối đa
                    if (discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                        discountAmount = voucher.getMaxDiscountAmount();
                    }

                    // Trừ số lượng voucher
                    voucher.setQuantity(voucher.getQuantity() - 1);
                    voucherRepository.save(voucher);
                }
            }
        }

        // 5. Tính tổng thanh toán cuối cùng
        BigDecimal finalAmount = totalProductsPrice.add(shippingFee).subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 6. Lưu đơn hàng (Header)
        Order order = new Order();
        order.setUser(user);
        order.setStatus("Chờ xác nhận");
        order.setNote(note);
        order.setShippingAddress(shippingAddress);
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingCity(city);
        order.setTotalProductsPrice(totalProductsPrice);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setVoucher(voucher);
        order.setFinalAmount(finalAmount);

        PaymentMethod pm = paymentMethodRepository.findById(paymentMethodId).orElse(null);
        order.setPaymentMethod(pm);

        Order savedOrder = orderRepository.save(order);

        // 7. Lưu chi tiết đơn hàng (Details)
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartDetail item : selectedItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getProduct().getSalePrice());
            detail.setTotalPrice(item.getProduct().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderDetails.add(detail);
        }
        orderDetailRepository.saveAll(orderDetails);

        // 8. Gửi email xác nhận đặt hàng
        try {
            if (user.getEmail() != null) {
                emailService.sendOrderStatusEmail(savedOrder);
            }
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }

        // 9. Xóa sản phẩm khỏi giỏ hàng
        cartDetailRepository.deleteAll(selectedItems);

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"));
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Thêm logic hoàn kho khi Admin hủy đơn
    @Override
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            String oldStatus = order.getStatus(); // Lưu trạng thái cũ trước khi cập nhật

            if ("Đã hủy".equals(status) && !"Đã hủy".equals(oldStatus)) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Product product = detail.getProduct();
                    product.setStock(product.getStock() + detail.getQuantity());
                    productRepository.save(product);
                }
            }

            // 1. Cập nhật trạng thái mới vào DB
            order.setStatus(status);
            orderRepository.save(order);

            // 2. Logic kích hoạt bảo hành (khi giao hàng thành công)
            boolean isNewStatusDelivered = status != null && status.contains("Đã giao");
            boolean isOldStatusNotDelivered = oldStatus == null || !oldStatus.contains("Đã giao");

            if (isNewStatusDelivered && isOldStatusNotDelivered) {
                try {
                    warrantyService.activateWarranty(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 3. Logic gửi mail thông báo trạng thái đơn hàng (đã giao, đang giao...)
            if (order.getUser() != null && order.getUser().getEmail() != null) {
                try {
                    Hibernate.initialize(order.getOrderDetails());
                    emailService.sendOrderStatusEmail(order);
                } catch (Exception e) {
                    System.out.println("Lỗi gửi mail đơn hàng: " + e.getMessage());
                }
            }

            // 4.Logic gửi mail tặng Voucher (Tích lũy)
            boolean isPaidOrSuccess = (status != null) && (status.contains("Đã thanh toán") || status.contains("Đã giao"));
            boolean wasNotPaidOrSuccess = (oldStatus == null) || (!oldStatus.contains("Đã thanh toán") && !oldStatus.contains("Đã giao"));

            if (isPaidOrSuccess && wasNotPaidOrSuccess) {
                if (order.getUser() != null && order.getUser().getEmail() != null) {
                    try {
                        BigDecimal totalSpent = calculateTotalSpent(order.getUser());
                        emailService.sendVoucherGiftNotification(order.getUser(), totalSpent);
                        System.out.println("Đã gửi mail tặng voucher cho user: " + order.getUser().getEmail());
                    } catch (Exception e) {
                        System.out.println("Lỗi gửi mail tặng voucher: " + e.getMessage());
                    }
                }
            }
        }
    }

    //Cho phép hủy cả đơn "Chờ thanh toán"
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        //Thêm điều kiện "Chờ thanh toán"
        if (order != null && ("Chờ xác nhận".equals(order.getStatus()) || "Chờ thanh toán".equals(order.getStatus()))) {
            order.setStatus("Đã hủy");
            orderRepository.save(order);

            // Hoàn lại tồn kho
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = detail.getProduct();
                product.setStock(product.getStock() + detail.getQuantity());
                productRepository.save(product);
            }
            // Gửi mail thông báo hủy
            try {
                emailService.sendOrderCancellationNotification(order);
            } catch (Exception e) {
                System.out.println("Lỗi mail hủy: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Object[]> getMonthlyRevenue() {
        return orderRepository.getMonthlyRevenue();
    }

    @Override
    public List<Order> searchOrders(String keyword, String status, String dateStr) {
        LocalDate searchDate = null;
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                searchDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                searchDate = null;
            }
        }
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        return orderRepository.searchOrders(keyword, status, searchDate);
    }

    @Override
    public BigDecimal calculateTotalSpent(User user) {
        BigDecimal total = orderRepository.sumTotalSpentByUser(user);
        return total == null ? BigDecimal.ZERO : total;
    }
}