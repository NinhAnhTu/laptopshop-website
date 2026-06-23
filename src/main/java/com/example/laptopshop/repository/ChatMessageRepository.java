package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Lấy toàn bộ lịch sử tin nhắn của một phòng chat, sắp xếp cũ -> mới
    @Query("SELECT m FROM ChatMessage m WHERE m.room.roomId = :roomId ORDER BY m.createdAt ASC")
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(@Param("roomId") Long roomId);
}