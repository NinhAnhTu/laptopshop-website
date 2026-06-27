package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.*;
import com.example.laptopshop.event.OrderStatusChangedEvent;
import com.example.laptopshop.facade.CheckoutFacade;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final WarrantyService warrantyService;

    private final CheckoutFacade checkoutFacade;

    private final ApplicationEventPublisher eventPublisher;

    private final TransactionRepository transactionRepository;
    @Override
    @Transactional
    public Order placeOrder(User user, String note, String shippingAddress,
                            String shippingName, String shippingPhone, Long cityId, Long paymentMethodId,
                            List<Long> selectedCartDetailIds, String voucherCode) {
        return checkoutFacade.processCheckout(
                user, note, shippingAddress, shippingName, shippingPhone,
                cityId, paymentMethodId, selectedCartDetailIds, voucherCode
        );
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

            // 2. PHÁT LOA THÔNG BÁO (Áp dụng Observer Pattern)
            // Không cần quan tâm ai gửi mail, ai làm bảo hành nữa, cứ quăng Event ra là xong!
            eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, oldStatus, status));
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

    @Override
    @Transactional
    public void saveTransaction(Long orderId, String vnpAmount, String vnpTransactionNo) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            Transaction transaction = new Transaction();
            transaction.setOrder(order);

            // Cài đặt PaymentMethod (2 = Chuyển khoản)
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setMethodId(2L);
            transaction.setPaymentMethod(paymentMethod);

            // VNPAY trả về số tiền nhân 100, nên ta phải chia lại cho 100
            BigDecimal amount = new BigDecimal(vnpAmount).divide(new BigDecimal(100));
            transaction.setAmount(amount);

            transaction.setStatus("SUCCESS");
            transaction.setPaymentDate(LocalDateTime.now());
            transaction.setInvoiceUrl(vnpTransactionNo); // Lưu mã giao dịch VNPAY tạm vào đây

            transactionRepository.save(transaction);
        }
    }
}