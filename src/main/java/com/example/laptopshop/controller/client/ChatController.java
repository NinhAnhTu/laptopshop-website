package com.example.laptopshop.controller.client;

import com.example.laptopshop.dto.ChatMessageDTO;
import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.ChatMessageService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatService;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDTO chatDto, Principal principal) {
        if (principal == null) return ResponseEntity.badRequest().body("Vui lòng đăng nhập");

        User sender = userService.getUserByEmail(principal.getName());
        User receiver = userService.getUserById(chatDto.getReceiverId());

        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(chatDto.getContent());

        chatService.saveMessage(msg);
        return ResponseEntity.ok("Sent");
    }

    @GetMapping("/history/{targetUserId}")
    public List<ChatMessage> getHistory(@PathVariable Long targetUserId, Principal principal) {
        if (principal == null) return null;

        // Lấy User đang đăng nhập (Chính là Admin ID 1)
        User currentUser = userService.getUserByEmail(principal.getName());

        // Lấy lịch sử giữa Admin ID 1 và Khách hàng (targetUserId)
        return chatService.getHistory(currentUser.getUserId(), targetUserId);
    }
}