package com.example.laptopshop.observer;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.event.OrderStatusChangedEvent;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EmailObserver {

    private final EmailService emailService;
    private final OrderService orderService; // Dùng để tính tổng tiền

    @EventListener
    @Transactional(readOnly = true)
    public void handleEmailNotifications(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        String oldStatus = event.getOldStatus();
        String newStatus = event.getNewStatus();

        if (order.getUser() == null || order.getUser().getEmail() == null) return;

        // 1. Gửi mail thông báo trạng thái
        try {
            System.out.println("👉 [Observer Email] Đang gửi mail cập nhật trạng thái đơn: " + order.getOrderId());
            Hibernate.initialize(order.getOrderDetails());
            emailService.sendOrderStatusEmail(order);
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail đơn hàng: " + e.getMessage());
        }
    }
}