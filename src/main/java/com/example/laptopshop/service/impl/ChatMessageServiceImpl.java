package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.ChatMessageRepository;
import com.example.laptopshop.repository.UserRepository;
import com.example.laptopshop.service.ChatBotService;
import com.example.laptopshop.service.ChatMessageService;
import com.example.laptopshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatBotService chatBotService;
    private final UserRepository userRepository;

    private static final long MAIN_ADMIN_ID = 1L;

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        Long receiverId = message.getReceiver().getUserId();
        Long senderId = message.getSender().getUserId();

        // 1. Gửi mail thông báo nếu khách nhắn cho Admin ID 1
        if (receiverId == MAIN_ADMIN_ID) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            boolean hasChattedToday = chatMessageRepository.existsBySender_UserIdAndReceiver_UserIdAndTimestampBetween(
                    senderId, MAIN_ADMIN_ID, startOfDay, endOfDay
            );
            if (!hasChattedToday) {
                emailService.sendNewMessageNotification(message);
            }
        }

        // 2. Lưu tin nhắn gốc
        ChatMessage savedMsg = chatMessageRepository.save(message);

        Long channelId = (senderId == MAIN_ADMIN_ID) ? receiverId : senderId;
        messagingTemplate.convertAndSend("/topic/messages/" + channelId, savedMsg);

        // 4. LOGIC CHATBOT (AI)
        // Chỉ kích hoạt khi người gửi KHÁCH (Không phải ID 1)
        if (senderId != MAIN_ADMIN_ID) {
            String userEmail = message.getSender().getEmail();

            // Gọi AI
            String botReplyContent = chatBotService.getAutoReply(message.getContent(), userEmail);

            if (botReplyContent != null) {
                ChatMessage botMsg = new ChatMessage();
                botMsg.setContent(botReplyContent);
                botMsg.setTimestamp(LocalDateTime.now());
                botMsg.setRead(false);

                // Set người gửi là Admin ID 1
                User adminUser = userRepository.findById(MAIN_ADMIN_ID).orElse(null);
                User clientUser = message.getSender();

                if (adminUser != null && clientUser != null) {
                    botMsg.setSender(adminUser);
                    botMsg.setReceiver(clientUser);

                    ChatMessage savedBotMsg = chatMessageRepository.save(botMsg);

                    // Gửi tin nhắn Bot qua Socket ngay lập tức
                    messagingTemplate.convertAndSend("/topic/messages/" + clientUser.getUserId(), savedBotMsg);
                }
            }
        }
        return savedMsg;
    }

    @Override
    public List<ChatMessage> getHistory(Long userId1, Long userId2) {
        return chatMessageRepository.findChatHistory(userId1, userId2);
    }

    @Override
    public List<User> getUsersWhoChatted() {
        return chatMessageRepository.findUsersWhoChatted();
    }
}