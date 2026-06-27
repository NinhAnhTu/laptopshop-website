package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.ChatRoom;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.ChatMessageRepository;
import com.example.laptopshop.repository.ChatRoomRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.ChatBotService;
import com.example.laptopshop.service.ChatMessageService;
import com.example.laptopshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final ChatBotService chatBotService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    @Override
    @Transactional
    public ChatMessage processUserMessage(User user, String content) {
        ChatRoom room = chatRoomRepository.findByCustomer(user).orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setCustomer(user);
            return chatRoomRepository.save(newRoom);
        });

        ChatMessage userMsg = new ChatMessage();
        userMsg.setRoom(room);
        userMsg.setSenderType("USER");
        userMsg.setSender(user);
        userMsg.setContent(content);
        chatMessageRepository.save(userMsg);

        // [SỬA LỖI Ở ĐÂY] Bắn DTO an toàn qua socket
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), convertToSafeDto(userMsg));

        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
        boolean needsHelp = false; // Biến theo dõi AI có kêu cứu không
        if (room.getStatus().equals("AI") || room.getStatus().equals("CLOSED")) {
            if (room.getStatus().equals("CLOSED")) room.setStatus("AI");

            // [MỚI] TRÍCH XUẤT 6 TIN NHẮN GẦN NHẤT LÀM TRÍ NHỚ CHO AI
            List<ChatMessage> history = getHistoryByRoomId(room.getRoomId());
            StringBuilder recentHistory = new StringBuilder();
            int startIdx = Math.max(0, history.size() - 6); // Lấy 6 tin nhắn cuối
            for (int i = startIdx; i < history.size(); i++) {
                ChatMessage m = history.get(i);
                recentHistory.append(m.getSenderType()).append(": ").append(m.getContent()).append("\n");
            }

            String aiReply = chatBotService.getAutoReply(content, user.getEmail(), recentHistory.toString());


            if (aiReply.contains("CONNECT_ADMIN") || aiReply.contains("Hệ thống AI đang quá tải")) {
                room.setStatus("WAITING_ADMIN");
                chatRoomRepository.save(room);
                sendSystemMessage(room, "Hệ thống đang kết nối bạn với nhân viên hỗ trợ. Vui lòng chờ trong giây lát...");
                needsHelp = true; // Kích hoạt báo động đỏ
            } else {
                sendAiMessage(room, aiReply);
            }
        }
        notifyAdmin(room, true, needsHelp);
        return userMsg;
    }

    @Override
    @Transactional
    public ChatMessage processAdminMessage(Long roomId, User admin, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        ChatMessage adminMsg = new ChatMessage();
        adminMsg.setRoom(room);
        adminMsg.setSenderType("ADMIN");
        adminMsg.setSender(admin);
        adminMsg.setContent(content);

        ChatMessage saved = chatMessageRepository.save(adminMsg);
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // [SỬA LỖI Ở ĐÂY]
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), convertToSafeDto(saved));
        return saved;
    }

    @Override
    @Transactional
    public void assignAdminToRoom(Long roomId, User admin) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        room.setStatus("CHATTING");
        room.setAssignedAdmin(admin);
        chatRoomRepository.save(room);
        sendSystemMessage(room, "Nhân viên " + admin.getFullname() + " đã tham gia cuộc trò chuyện.");

        notifyAdmin(room, false, false); // Cập nhật lại UI Admin
    }

    @Override
    @Transactional
    public void closeRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        room.setStatus("CLOSED");
        room.setAssignedAdmin(null);
        chatRoomRepository.save(room);
        sendSystemMessage(room, "Cuộc trò chuyện đã kết thúc. Trợ lý AI đã quay trở lại.");

        notifyAdmin(room, false, false); // Cập nhật lại UI Admin
    }

    @Override
    public List<ChatMessage> getHistoryByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // --- HÀM ÉP KIỂU AN TOÀN TRÁNH LỖI TRÀN BỘ NHỚ KHI GỬI SOCKET ---
    private Map<String, Object> convertToSafeDto(ChatMessage msg) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", msg.getId());
        map.put("senderType", msg.getSenderType());
        map.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now());

        String rawContent = msg.getContent();
        List<Map<String, Object>> productList = new java.util.ArrayList<>();

        // Quét TẤT CẢ các thẻ [PRODUCT:id] có trong câu
        if (rawContent != null) {
            Pattern pattern = Pattern.compile("\\[PRODUCT:(\\d+)\\]");
            Matcher matcher = pattern.matcher(rawContent);
            while (matcher.find()) {
                Long productId = Long.parseLong(matcher.group(1));
                Product p = productRepository.findById(productId).orElse(null);
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
            // Xóa tất cả các thẻ [PRODUCT:123] ra khỏi đoạn text hiển thị
            rawContent = rawContent.replaceAll("\\[PRODUCT:\\d+\\]", "").trim();
        }

        map.put("content", rawContent);
        map.put("suggestedProducts", productList); // [MỚI] Trả về một Mảng (List) các sản phẩm
        return map;
    }

    private void sendAiMessage(ChatRoom room, String rawAiContent) {
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setRoom(room);
        aiMsg.setSenderType("AI");
        aiMsg.setSender(null);

        // LƯU NGUYÊN BẢN (có chứa thẻ [PRODUCT:id]) VÀO DATABASE
        aiMsg.setContent(rawAiContent);
        chatMessageRepository.save(aiMsg);

        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), convertToSafeDto(aiMsg));
    }

    private void sendSystemMessage(ChatRoom room, String content) {
        ChatMessage sysMsg = new ChatMessage();
        sysMsg.setRoom(room);
        sysMsg.setSenderType("SYSTEM");
        sysMsg.setSender(null);
        sysMsg.setContent(content);
        chatMessageRepository.save(sysMsg);

        // [SỬA LỖI Ở ĐÂY]
        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), convertToSafeDto(sysMsg));
    }

    // --- [MỚI] HÀM PHÁT TÍN HIỆU REAL-TIME CHO ADMIN ---
    private void notifyAdmin(ChatRoom room, boolean isNewUserMessage, boolean needsHelp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomId", room.getRoomId());
        payload.put("status", room.getStatus());
        payload.put("isNewUserMessage", isNewUserMessage);
        payload.put("needsHelp", needsHelp);
        messagingTemplate.convertAndSend("/topic/admin", payload);
    }
}