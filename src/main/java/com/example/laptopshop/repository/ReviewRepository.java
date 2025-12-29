package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductProductIdOrderByCreatedAtDesc(Long productId);

    Page<Review> findByProductProductId(Long productId, Pageable pageable);
    @Query("SELECT r FROM Review r LEFT JOIN r.reply rp WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(r.user.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.product.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:rating IS NULL OR r.rating = :rating) " +
            "AND (:status IS NULL OR :status = '' OR (:status = 'replied' AND rp IS NOT NULL) OR (:status = 'pending' AND rp IS NULL)) " +
            "ORDER BY r.createdAt DESC")
    List<Review> searchReviews(@Param("keyword") String keyword,
                               @Param("rating") Integer rating,
                               @Param("status") String status);
}