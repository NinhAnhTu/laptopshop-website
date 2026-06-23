package com.example.laptopshop.strategy;

import com.example.laptopshop.entity.Order;

public interface PaymentStrategy {
    /**
     * Hàm xử lý thanh toán. Trả về String (có thể là câu thông báo,
     * hoặc URL chuyển hướng sang cổng thanh toán như VNPay, Momo)
     */
    String processPayment(Order order);
}