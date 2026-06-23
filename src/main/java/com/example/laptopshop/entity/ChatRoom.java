package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @OneToOne // Mỗi khách chỉ có đúng 1 Room duy nhất trong suốt vòng đời
    @JoinColumn(name = "customer_id", unique = true, nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    @Column(name = "status", columnDefinition = "ENUM('AI', 'WAITING_ADMIN', 'CHATTING', 'CLOSED') DEFAULT 'AI'")
    private String status = "AI";

    @Column(name = "conversation_summary", columnDefinition = "TEXT")
    private String conversationSummary; // Lưu tóm tắt lịch sử chat cho AI đọc

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}