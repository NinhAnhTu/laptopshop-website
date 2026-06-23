package com.example.laptopshop.facade;

import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.strategy.PaymentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckoutFacade {

    // --- CÁC PHÂN HỆ (SUBSYSTEMS) ĐƯỢC FACADE CHE GIẤU VÀ ĐIỀU PHỐI ---
    private final CartDetailRepository cartDetailRepository;
    private final CityRepository cityRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final PaymentContext paymentContext;
    /**
     * ĐÂY LÀ HÀM DUY NHẤT GIAO TIẾP VỚI BÊN NGOÀI
     * Controller chỉ cần gọi hàm này, mọi rắc rối Facade sẽ tự lo.
     */
    @Transactional
    public Order processCheckout(User user, String note, String shippingAddress,
                                 String shippingName, String shippingPhone, Long cityId,
                                 Long paymentMethodId, List<Long> selectedCartDetailIds,
                                 String voucherCode) {

        // 1. Phân hệ Giỏ hàng
        List<CartDetail> items = cartDetailRepository.findAllById(selectedCartDetailIds);
        if (items.isEmpty()) throw new RuntimeException("Không có sản phẩm nào được chọn!");

        // 2. Phân hệ Tính Giá & Vận Chuyển (Pricing Subsystem)
        BigDecimal totalProductsPrice = calculateTotal(items);
        BigDecimal shippingFee = calculateShipping(cityId);
        Voucher voucher = validateAndGetVoucher(voucherCode, totalProductsPrice);
        BigDecimal discountAmount = calculateDiscount(voucher, totalProductsPrice);

        BigDecimal finalAmount = totalProductsPrice.add(shippingFee).subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        // 3. Phân hệ Lưu Đơn Hàng (Sử dụng Builder Pattern từ bước trước)
        PaymentMethod pm = paymentMethodRepository.findById(paymentMethodId).orElse(null);
        City city = cityRepository.findById(cityId).orElse(null);

        Order order = Order.builder()
                .user(user).status("Chờ xác nhận").note(note)
                .shippingAddress(shippingAddress).shippingName(shippingName).shippingPhone(shippingPhone)
                .shippingCity(city).totalProductsPrice(totalProductsPrice).shippingFee(shippingFee)
                .discountAmount(discountAmount).voucher(voucher).finalAmount(finalAmount).paymentMethod(pm)
                .build();
        Order savedOrder = orderRepository.save(order);

        // 4. Phân hệ Kho hàng (Inventory Subsystem)
        deductInventoryAndSaveDetails(savedOrder, items);

        // 5. Phân hệ Thông báo (Notification Subsystem)
        sendOrderEmail(user, savedOrder);

        // 6. Dọn dẹp giỏ hàng
        cartDetailRepository.deleteAll(items);

        // 7. BÀN GIAO CHO CHIẾN LƯỢC THANH TOÁN
        Long methodId = pm.getMethodId(); // Lấy ID (1 hoặc 2) từ PaymentMethod
        String redirectUrl = paymentContext.executePayment(methodId, savedOrder);
        return savedOrder;
    }


    // =========================================================================
    // CÁC HÀM LOGIC KỸ THUẬT NỘI BỘ (ĐƯỢC GIẤU KÍN TRONG FACADE)
    // =========================================================================

    private BigDecimal calculateTotal(List<CartDetail> items) {
        return items.stream()
                .map(item -> item.getProduct().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateShipping(Long cityId) {
        City city = cityRepository.findById(cityId).orElseThrow(() -> new RuntimeException("City not found"));
        ShippingRate rate = shippingRateRepository.findByRegionRegionId(city.getRegion().getRegionId()).orElse(null);
        return (rate != null) ? rate.getBaseFee() : BigDecimal.valueOf(50000);
    }

    private Voucher validateAndGetVoucher(String code, BigDecimal totalProductsPrice) {
        if (code == null || code.trim().isEmpty()) return null;
        Voucher voucher = voucherRepository.findByCode(code).orElse(null);
        if (voucher != null) {
            if (!"active".equals(voucher.getStatus()) || voucher.getQuantity() <= 0
                    || LocalDateTime.now().isBefore(voucher.getStartDate()) || LocalDateTime.now().isAfter(voucher.getEndDate())) {
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc hết lượt!");
            }
            if (totalProductsPrice.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu!");
            }
            // Trừ số lượng voucher
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherRepository.save(voucher);
        }
        return voucher;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal totalProductsPrice) {
        if (voucher == null) return BigDecimal.ZERO;
        BigDecimal percent = voucher.getDiscountPercent().divide(BigDecimal.valueOf(100));
        BigDecimal discount = totalProductsPrice.multiply(percent);
        return discount.compareTo(voucher.getMaxDiscountAmount()) > 0 ? voucher.getMaxDiscountAmount() : discount;
    }

    private void deductInventoryAndSaveDetails(Order savedOrder, List<CartDetail> items) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartDetail item : items) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm [" + product.getProductName() + "] không đủ số lượng!");
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(product.getSalePrice());
            detail.setTotalPrice(product.getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderDetails.add(detail);

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        orderDetailRepository.saveAll(orderDetails);
    }

    private void sendOrderEmail(User user, Order order) {
        try {
            if (user.getEmail() != null) emailService.sendOrderStatusEmail(order);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }
    }
}