package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
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
    private final long MAIN_ADMIN_ID = 1L; // ID cố định của Admin

    @GetMapping
    public String getAdminChatPage(Model model, @RequestParam(required = false) Long userId) {
        // 1. Lấy danh sách khách hàng đã chat để hiển thị Sidebar
        List<User> users = chatService.getUsersWhoChatted();
        model.addAttribute("users", users);

        // 2. Xác định xem sẽ hiển thị tin nhắn của ai
        // Nếu có userId trên URL (khi bấm chọn) -> dùng userId đó
        // Nếu không có (vừa vào trang) -> lấy user đầu tiên trong danh sách
        Long targetUserId = userId;
        if (targetUserId == null && !users.isEmpty()) {
            targetUserId = users.get(0).getUserId();
        }

        // 3. Lấy lịch sử tin nhắn (Nếu đã xác định được khách hàng)
        if (targetUserId != null) {
            List<ChatMessage> messages = chatService.getHistory(MAIN_ADMIN_ID, targetUserId);
            model.addAttribute("messages", messages);       // Đẩy list tin nhắn sang View
            model.addAttribute("targetUserId", targetUserId); // Để highlight user đang chọn
        } else {
            model.addAttribute("messages", Collections.emptyList());
        }

        // 4. Active menu sidebar
        model.addAttribute("activePage", "chat");

        return "admin/chat/dashboard";
    }
}