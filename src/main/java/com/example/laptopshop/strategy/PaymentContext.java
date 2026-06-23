package com.example.laptopshop.strategy;

import com.example.laptopshop.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentContext {

    // Spring Boot tự động đưa CodPaymentStrategy (Key "1") và BankTransferStrategy (Key "2") vào Map này
    private final Map<String, PaymentStrategy> strategies;

    public String executePayment(Long methodId, Order order) {
        // Lấy chiến lược dựa theo ID
        PaymentStrategy strategy = strategies.get(String.valueOf(methodId));

        if (strategy == null) {
            throw new IllegalArgumentException("Hệ thống chưa hỗ trợ phương thức thanh toán ID: " + methodId);
        }

        return strategy.processPayment(order);
    }
}