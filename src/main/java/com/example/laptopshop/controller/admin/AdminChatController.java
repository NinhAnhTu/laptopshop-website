package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.ChatRoom;
import com.example.laptopshop.repository.ChatRoomRepository;
import com.example.laptopshop.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatMessageService chatService;
    private final ChatRoomRepository chatRoomRepository;

    @GetMapping
    public String getAdminChatPage(Model model, @RequestParam(required = false) Long roomId) {

        // 1. Lấy danh sách các phòng chat
        List<ChatRoom> rooms = chatRoomRepository.findAll(Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        model.addAttribute("rooms", rooms);

        // Lấy tin nhắn cuối cùng + số tin nhắn CHƯA ĐỌC cho mỗi phòng
        Map<Long, ChatMessage> lastMessages = new HashMap<>();
        Map<Long, Integer> unreadCounts = new HashMap<>();
        for (ChatRoom room : rooms) {
            List<ChatMessage> history = chatService.getHistoryByRoomId(room.getRoomId());
            if (!history.isEmpty()) {
                // Lấy tin nhắn cuối cùng trong danh sách lịch sử
                lastMessages.put(room.getRoomId(), history.get(history.size() - 1));
            }

            // Chưa đọc = tổng tin của khách - số tin của khách mà admin đã đọc
            int customerCount = countCustomerMessages(history);
            int readCount = room.getAdminReadCount() == null ? 0 : room.getAdminReadCount();
            unreadCounts.put(room.getRoomId(), Math.max(0, customerCount - readCount));
        }
        model.addAttribute("lastMessages", lastMessages);
        model.addAttribute("unreadCounts", unreadCounts);

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
            model.addAttribute("currentRoom", currentRoom);
        } else {
            model.addAttribute("messages", Collections.emptyList());
        }

        model.addAttribute("activePage", "chat");
        return "admin/chat/dashboard";
    }

    // Admin mở phòng chat (hoặc đang xem phòng mà khách vừa nhắn) -> đánh dấu đã đọc toàn bộ tin của khách
    @PostMapping("/read/{roomId}")
    @ResponseBody
    public ResponseEntity<Void> markRoomAsRead(@PathVariable Long roomId) {
        int customerCount = countCustomerMessages(chatService.getHistoryByRoomId(roomId));
        int updated = chatRoomRepository.updateAdminReadCount(roomId, customerCount);
        return updated > 0 ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Đếm số tin nhắn do KHÁCH HÀNG gửi (loại trừ ADMIN, AI, SYSTEM)
    private int countCustomerMessages(List<ChatMessage> history) {
        int count = 0;
        for (ChatMessage m : history) {
            if (m.getSenderType() == null) continue;
            String type = String.valueOf(m.getSenderType());
            if (!"ADMIN".equals(type) && !"AI".equals(type) && !"SYSTEM".equals(type)) {
                count++;
            }
        }
        return count;
    }
}