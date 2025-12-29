package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.Review;
import com.example.laptopshop.entity.ReviewReply;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.OrderDetailRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.repository.ReviewReplyRepository;
import com.example.laptopshop.repository.ReviewRepository;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.GeminiReviewService;
import com.example.laptopshop.service.ReviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final EmailService emailService;
    private final GeminiReviewService geminiReviewService;

    // --- LOGIC CỦA KHÁCH HÀNG ---

    @Override
    @Transactional
    public void saveReview(User user, Long productId, String comment, int rating) {
        // 1. Kiểm tra xem user đã mua và nhận hàng chưa
        boolean isBuys = orderDetailRepository.existsByOrderUserUserIdAndProductProductIdAndOrderStatus(
                user.getUserId(), productId, "Đã giao");

        if (!isBuys) {
            throw new RuntimeException("Bạn phải mua và nhận hàng thành công mới được đánh giá!");
        }

        // 2. Nếu thỏa mãn thì lưu
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setComment(comment);
            review.setRating(rating);
            review.setIsVerifiedPurchase(true);

            // Lưu xuống DB để có ID và đối tượng chuẩn
            Review savedReview = reviewRepository.save(review);

            processAutoReviewAction(savedReview);
        }
    }

    /**
     * Hàm xử lý tự động: AI trả lời hoặc Báo động Admin
     * Chạy bất đồng bộ (@Async)
     */
    @Async
    public void processAutoReviewAction(Review review) {
        // TRƯỜNG HỢP 1: 5 SAO -> AI TRẢ LỜI
        if (review.getRating() == 5) {
            try {
                // Gọi Gemini sinh nội dung
                String aiReply = geminiReviewService.generateReply(
                        review.getUser().getFullname(),
                        review.getProduct().getProductName(),
                        review.getComment()
                );

                if (aiReply != null && !aiReply.isBlank()) {
                    replyToReview(review.getReviewId(), aiReply);

                    System.out.println("Gemini đã trả lời review ID: " + review.getReviewId());
                }
            } catch (Exception e) {
                System.out.println("Lỗi AI Auto-Reply: " + e.getMessage());
            }
        }
        // TRƯỜNG HỢP 2: DƯỚI 5 SAO -> GỬI MAIL BÁO ADMIN
        else {
            emailService.sendAdminReviewAlert(review);
        }
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public Page<Review> getReviewsByProductIds(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return reviewRepository.findByProductProductId(productId, pageable);
    }

    // --- LOGIC CỦA ADMIN ---

    @Override
    public List<Review> getAllReviews() {
        // Lấy tất cả review, sắp xếp mới nhất lên đầu để Admin dễ quản lý
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<Review> getAllReviewss(String keyword, Integer rating, String status) {
        return reviewRepository.searchReviews(keyword, rating, status);
    }

    @Override
    @Transactional
    public void replyToReview(Long reviewId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá ID: " + reviewId));

        ReviewReply reply = review.getReply();
        if (reply == null) {
            reply = new ReviewReply();
            reply.setReview(review);
        }

        reply.setContent(content);

        // Lưu reply
        reviewReplyRepository.save(reply);

        // Cập nhật lại object review
        review.setReply(reply);

        emailService.sendReviewReplyEmail(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá ID: " + reviewId));

        // Gửi mail thông báo lý do xóa TRƯỚC KHI xóa dữ liệu
        emailService.sendReviewRemovedEmail(review, reason);

        // Xóa review (Reply đi kèm cũng sẽ bị xóa nhờ CascadeType.ALL bên Entity Review)
        reviewRepository.delete(review);
    }
}