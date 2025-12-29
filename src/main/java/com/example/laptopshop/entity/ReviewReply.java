package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_replies")
@Getter @Setter
public class ReviewReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime replyDate;

    // Quan hệ 1-1 với Review
    @OneToOne
    // name = "review_id": Tên cột khóa ngoại trong bảng review_replies
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
}