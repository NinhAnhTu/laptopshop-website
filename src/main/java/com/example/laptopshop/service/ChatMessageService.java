package com.example.laptopshop.service;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User; // [QUAN TRỌNG] Thêm dòng này
import java.util.List;

public interface ChatMessageService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getHistory(Long userId1, Long userId2);
    List<User> getUsersWhoChatted();

}