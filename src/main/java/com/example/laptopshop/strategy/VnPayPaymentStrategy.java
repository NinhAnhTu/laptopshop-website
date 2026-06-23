package com.example.laptopshop.strategy;

import com.example.laptopshop.entity.Order;
import org.springframework.stereotype.Component;

@Component("2")
public class VnPayPaymentStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Order order) {
        // Nơi đây bạn sẽ gọi các hàm băm mã SHA-512, tạo URL của VNPay
        System.out.println("Đang tạo URL thanh toán VNPay cho đơn: " + order.getOrderId());

        String vnpayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?amount=" + order.getFinalAmount();

        return vnpayUrl; // Trả về URL để Controller redirect khách hàng sang web VNPay
    }
}