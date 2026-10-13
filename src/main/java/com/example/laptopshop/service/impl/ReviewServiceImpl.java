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

        // 2. Tìm kiếm sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        // 3. LOGIC UPSERT: Kiểm tra xem User này đã từng đánh giá sản phẩm này chưa
        Review existingReview = reviewRepository.findByUserAndProduct(user, product);
        Review reviewToProcess;

        if (existingReview != null) {
            // [CẬP NHẬT ĐÁNH GIÁ CŨ]
            existingReview.setComment(comment);
            existingReview.setRating(rating);
            existingReview.setCreatedAt(java.time.LocalDateTime.now()); // Reset lại thời gian về hiện tại
            reviewToProcess = reviewRepository.save(existingReview);

            // Xóa câu trả lời cũ của Admin (nếu có) vì nội dung đánh giá đã thay đổi
            if(existingReview.getReply() != null) {
                reviewReplyRepository.delete(existingReview.getReply());
                existingReview.setReply(null);
            }
        } else {
            // [TẠO ĐÁNH GIÁ MỚI]
            Review newReview = new Review();
            newReview.setUser(user);
            newReview.setProduct(product);
            newReview.setComment(comment);
            newReview.setRating(rating);
            newReview.setIsVerifiedPurchase(true);
            reviewToProcess = reviewRepository.save(newReview);
        }

        // 4. Tính toán và cập nhật lại điểm Rating trung bình cho Product
        // (Bắt buộc phải gọi saveAndFlush để DB ghi nhận review vừa thêm/sửa trước khi tính trung bình)
        reviewRepository.flush();

        List<Review> allReviews = reviewRepository.findByProductProductIdOrderByCreatedAtDesc(productId);
        double avgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Làm tròn đến 1 chữ số thập phân (ví dụ: 4.5, 4.8)
        double roundedRating = Math.round(avgRating * 10.0) / 10.0;
        product.setRating(roundedRating);
        productRepository.save(product);

        // 5. Gọi hàm AI Tự động phản hồi (Chỉ chạy nếu là đánh giá mới hoặc vừa được cập nhật)
        processAutoReviewAction(reviewToProcess);
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

    @Override
    public Review findReviewByUserAndProduct(User user, Product product) {
        if (user == null || product == null) return null;
        // Bắt buộc bạn phải có hàm findByUserAndProduct bên trong ReviewRepository nhé
        return reviewRepository.findByUserAndProduct(user, product);
    }
}