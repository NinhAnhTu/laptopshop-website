package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    Order placeOrder(User user, String note, String shippingAddress,
                     String shippingName, String shippingPhone, Long cityId, Long paymentMethodId,
                     List<Long> selectedCartDetailIds, String voucherCode);

    List<Order> getOrdersByUser(User user);

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    void updateOrderStatus(Long orderId, String status);

    List<Object[]> getMonthlyRevenue();

    void cancelOrder(Long orderId);

    List<Order> searchOrders(String keyword, String status, String dateStr);

    BigDecimal calculateTotalSpent(User user);
}