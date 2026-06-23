package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.ChatRoom;
import com.example.laptopshop.repository.ChatRoomRepository;
import com.example.laptopshop.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatMessageService chatService;
    private final ChatRoomRepository chatRoomRepository; // [MỚI] Sử dụng Repository của Room

    @GetMapping
    public String getAdminChatPage(Model model, @RequestParam(required = false) Long roomId) {

        // 1. Lấy danh sách các phòng chat đang cần hỗ trợ (WAITING_ADMIN, CHATTING)
        List<ChatRoom> rooms = chatRoomRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "lastMessageAt"));
        model.addAttribute("rooms", rooms);

        // 2. Xác định xem Admin đang bấm vào xem phòng nào
        Long targetRoomId = roomId;
        if (targetRoomId == null && !rooms.isEmpty()) {
            targetRoomId = rooms.get(0).getRoomId();
        }

        // 3. Lấy lịch sử tin nhắn của phòng đó
        if (targetRoomId != null) {
            List<ChatMessage> messages = chatService.getHistoryByRoomId(targetRoomId);
            ChatRoom currentRoom = chatRoomRepository.findById(targetRoomId).orElse(null);

            model.addAttribute("messages", messages);
            model.addAttribute("targetRoomId", targetRoomId);
            model.addAttribute("currentRoom", currentRoom); // Gửi thêm info phòng để check trạng thái
        } else {
            model.addAttribute("messages", Collections.emptyList());
        }

        model.addAttribute("activePage", "chat");
        return "admin/chat/dashboard";
    }
}