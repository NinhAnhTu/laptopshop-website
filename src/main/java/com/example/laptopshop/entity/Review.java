package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter @Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer rating;
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private Boolean isVerifiedPurchase;
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL)
    private ReviewReply reply;
}
