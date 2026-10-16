package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.ChatRoom;
import com.example.laptopshop.entity.Warranty;
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

    List<ChatMessage> findTop10ByRoomOrderByCreatedAtDescIdDesc(ChatRoom room);
    // WarrantyRepository
}