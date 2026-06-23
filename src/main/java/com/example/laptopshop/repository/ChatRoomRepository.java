package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ChatRoom;
import com.example.laptopshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Tìm phòng chat của một khách hàng cụ thể
    Optional<ChatRoom> findByCustomer(User customer);

    // Dành cho Admin: Lấy danh sách các phòng chat đang cần hỗ trợ hoặc đang chat
    @Query("SELECT r FROM ChatRoom r WHERE r.status IN ('WAITING_ADMIN', 'CHATTING') ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findActiveRoomsForAdmin();

    // Dành cho Admin: Tìm các phòng mà Admin này đang phụ trách
    List<ChatRoom> findByAssignedAdminOrderByLastMessageAtDesc(User admin);
}