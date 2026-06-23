package com.example.laptopshop.service;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
import java.util.List;

public interface ChatMessageService {
    // Khách hàng gửi tin nhắn
    ChatMessage processUserMessage(User user, String content);

    // Admin gửi tin nhắn
    ChatMessage processAdminMessage(Long roomId, User admin, String content);

    // Admin tiếp nhận phòng chat
    void assignAdminToRoom(Long roomId, User admin);

    // Admin kết thúc phiên
    void closeRoom(Long roomId);

    List<ChatMessage> getHistoryByRoomId(Long roomId);
}