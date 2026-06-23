package com.example.laptopshop.strategy;

import com.example.laptopshop.entity.Order;
import org.springframework.stereotype.Component;

@Component("1") // Tên component phải trùng với mã PaymentMethod trong Database
public class CodPaymentStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Order order) {
        // Logic của COD rất đơn giản, chỉ cần báo thành công
        System.out.println("Đang xử lý thanh toán COD cho đơn hàng: " + order.getOrderId());
        return "/checkout/success"; // Trả về trang cảm ơn
    }
}