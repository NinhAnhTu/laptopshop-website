
package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Lấy lịch sử chat giữa 2 người (Sắp xếp cũ -> mới)
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.userId = :userId1 AND m.receiver.userId = :userId2) OR " +
            "(m.sender.userId = :userId2 AND m.receiver.userId = :userId1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 2. Lấy danh sách khách hàng đã từng nhắn tin (để hiện bên cột trái Admin)
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.sender.userType.typeName = 'Customer'")
    List<User> findUsersWhoChatted();

    // 3. Kiểm tra xem hôm nay đã nhắn chưa (để hạn chế spam mail)
    boolean existsBySender_UserIdAndReceiver_UserIdAndTimestampBetween(Long senderId, Long receiverId, LocalDateTime start, LocalDateTime end);
}