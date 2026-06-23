package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.ChatRoomRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.ChatMessageService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatService;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductRepository productRepository;
    // Hàm tiện ích lấy User đang đăng nhập
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        if (principal instanceof OAuth2AuthenticationToken) {
            email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
        }
        return userService.findByEmail(email);
    }

    // --- HÀM ÉP KIỂU AN TOÀN TRÁNH VÒNG LẶP JSON ---
    private Map<String, Object> convertToSafeDto(ChatMessage msg) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", msg.getId());
        map.put("senderType", msg.getSenderType());
        map.put("createdAt", msg.getCreatedAt());

        String rawContent = msg.getContent();
        List<Map<String, Object>> productList = new java.util.ArrayList<>();

        if (rawContent != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[PRODUCT:(\\d+)\\]");
            java.util.regex.Matcher matcher = pattern.matcher(rawContent);
            while (matcher.find()) {
                Long productId = Long.parseLong(matcher.group(1));
                // Gọi tới ProductRepository (Bạn nhớ tiêm/khai báo ProductRepository ở trên cùng file này nhé)
                com.example.laptopshop.entity.Product p = productRepository.findById(productId).orElse(null);
                if (p != null) {
                    Map<String, Object> pMap = new java.util.HashMap<>();
                    pMap.put("productName", p.getProductName());
                    pMap.put("salePrice", p.getSalePrice());
                    pMap.put("slug", p.getSlug());
                    if (p.getImages() != null && !p.getImages().isEmpty()) {
                        pMap.put("images", List.of(Map.of("url", p.getImages().get(0).getUrl())));
                    }
                    productList.add(pMap);
                }
            }
            rawContent = rawContent.replaceAll("\\[PRODUCT:\\d+\\]", "").trim();
        }

        map.put("content", rawContent);
        map.put("suggestedProducts", productList);
        return map;
    }

    // 1. Dành cho Khách hàng gửi tin nhắn
    @PostMapping("/send/user")
    public ResponseEntity<?> sendUserMessage(@RequestBody Map<String, String> payload, Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return ResponseEntity.badRequest().body("Vui lòng đăng nhập để chat!");
        ChatMessage msg = chatService.processUserMessage(user, payload.get("content"));
        return ResponseEntity.ok(msg);
    }

    // 2. Dành cho Admin gửi tin nhắn
    @PostMapping("/send/admin/{roomId}")
    public ResponseEntity<?> sendAdminMessage(@PathVariable Long roomId, @RequestBody Map<String, String> payload, Principal principal) {
        User admin = getUserFromPrincipal(principal);
        if (admin == null) return ResponseEntity.badRequest().body("Lỗi xác thực Admin");
        ChatMessage msg = chatService.processAdminMessage(roomId, admin, payload.get("content"));
        return ResponseEntity.ok(msg);
    }

    // 3. Admin bấm nút "Tham gia chat"
    @PostMapping("/assign/{roomId}")
    public ResponseEntity<?> assignAdmin(@PathVariable Long roomId, Principal principal) {
        User admin = getUserFromPrincipal(principal);
        if (admin == null) return ResponseEntity.badRequest().body("Lỗi xác thực Admin");
        chatService.assignAdminToRoom(roomId, admin);
        return ResponseEntity.ok("Assigned");
    }

    // 4. Admin bấm nút "Kết thúc hỗ trợ"
    @PostMapping("/close/{roomId}")
    public ResponseEntity<?> closeRoom(@PathVariable Long roomId) {
        chatService.closeRoom(roomId);
        return ResponseEntity.ok("Closed");
    }

    // 5. [ĐÃ SỬA] API Load lịch sử chat cho Admin
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getHistory(@PathVariable Long roomId) {
        List<Map<String, Object>> safeHistory = chatService.getHistoryByRoomId(roomId)
                .stream().map(this::convertToSafeDto).collect(Collectors.toList());
        return ResponseEntity.ok(safeHistory);
    }

    // 6. [ĐÃ SỬA] API Load lịch sử chat cho Khách
    @GetMapping("/my-room")
    public ResponseEntity<?> getMyRoom(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return ResponseEntity.status(401).body("Not logged in");

        // [FIX LỖI CỐT LÕI]: Tự động tạo phòng cho khách ngay khi họ vừa mở hộp chat
        com.example.laptopshop.entity.ChatRoom room = chatRoomRepository.findByCustomer(user).orElseGet(() -> {
            com.example.laptopshop.entity.ChatRoom newRoom = new com.example.laptopshop.entity.ChatRoom();
            newRoom.setCustomer(user);
            newRoom.setStatus("AI");
            return chatRoomRepository.save(newRoom);
        });

        List<Map<String, Object>> safeHistory = chatService.getHistoryByRoomId(room.getRoomId())
                .stream().map(this::convertToSafeDto).collect(Collectors.toList());

        return ResponseEntity.ok(java.util.Map.of(
                "hasRoom", true,
                "roomId", room.getRoomId(),
                "status", room.getStatus(),
                "history", safeHistory
        ));
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(Map<String, Object> payload) {
        Long roomId = Long.valueOf(payload.get("roomId").toString());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", payload);
    }
}