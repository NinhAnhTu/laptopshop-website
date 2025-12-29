package com.example.laptopshop.service;

import com.example.laptopshop.entity.Review;
import com.example.laptopshop.entity.User;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ReviewService {
    void saveReview(User user, Long productId, String comment, int rating);
    List<Review> getReviewsByProductId(Long productId);
    Page<Review> getReviewsByProductIds(Long productId, int page, int size);
    List<Review> getAllReviews(); // Lấy tất cả review để hiển thị trang Admin
    void replyToReview(Long reviewId, String content); // Admin trả lời
    void deleteReview(Long reviewId, String reason);   // Admin xóa
    List<Review> getAllReviewss(String keyword, Integer rating, String status);
}