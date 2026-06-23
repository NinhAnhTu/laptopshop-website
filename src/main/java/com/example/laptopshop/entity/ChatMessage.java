package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private ChatRoom room;

    @Column(name = "sender_type", nullable = false)
    private String senderType; // 'USER', 'ADMIN', 'AI', 'SYSTEM'

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnore
    private User sender; // Null nếu là AI hoặc SYSTEM

    @Column(name = "message_type")
    private String messageType = "TEXT"; // 'TEXT', 'IMAGE', 'FILE', 'SYSTEM'

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    // [MỚI TÍCH HỢP] Dùng để đính kèm Card sản phẩm vào tin nhắn
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product suggestedProduct;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}