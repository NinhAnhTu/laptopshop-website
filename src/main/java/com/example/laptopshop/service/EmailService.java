package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.Warranty;
import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.Review;

import java.math.BigDecimal;

public interface EmailService {
    void sendOrderStatusEmail(Order order);
    void sendResetPasswordEmail(String to, String token);
    void sendNewPasswordEmail(String to, String password);
    void sendWarrantyExpirationEmail(Warranty warranty);
    void sendNewMessageNotification(ChatMessage message);
    void sendOrderCancellationNotification(Order order);
    void sendVoucherGiftNotification(User user, BigDecimal totalSpent);
    void sendReviewReplyEmail(Review review);
    void sendReviewRemovedEmail(Review review, String reason);
    void sendAdminReviewAlert(com.example.laptopshop.entity.Review review);
}