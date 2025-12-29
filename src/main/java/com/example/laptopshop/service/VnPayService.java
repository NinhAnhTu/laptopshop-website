package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface VnPayService {
    String createVnPayPayment(HttpServletRequest request, Order order);
    int orderReturn(HttpServletRequest request);
}