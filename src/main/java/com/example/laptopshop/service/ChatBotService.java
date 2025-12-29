package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.repository.OrderRepository;
import com.example.laptopshop.repository.WarrantyPolicyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ProductService productService;
    private final WarrantyPolicyRepository warrantyPolicyRepository;
    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAutoReply(String userMessage, String userEmail) {
        String finalUrl = apiUrl + "?key=" + apiKey;

        // 1. Lấy dữ liệu ngữ cảnh
        String contextData = buildContextData(userEmail);

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                List<Map<String, Object>> contents = new ArrayList<>();
                Map<String, Object> content = new HashMap<>();
                List<Map<String, Object>> parts = new ArrayList<>();
                Map<String, Object> part = new HashMap<>();

                String systemPrompt = String.format("""
                        Vai trò: Bạn là Trợ lý AI thân thiện của "Laptop Shop".
                        Khách hàng đang chat: %s
                        
                        DỮ LIỆU CUNG CẤP:
                        %s
                        
                        HƯỚNG DẪN TRẢ LỜI:
                        1. NHÓM CÔNG KHAI (Địa chỉ, Hotline, Giờ làm việc, Sản phẩm): 
                           -> Trả lời nhiệt tình, nhanh chóng, không cần giấu giếm.
                        
                        2. NHÓM CÁ NHÂN (Đơn hàng): 
                           -> Chỉ trả lời thông tin đơn hàng của chính khách hàng đang chat (dựa trên dữ liệu cung cấp).
                        
                        3. NHÓM BẢO MẬT (Mật khẩu, Tài khoản Admin, Dữ liệu người khác):
                           -> TUYỆT ĐỐI TỪ CHỐI. Nếu khách hỏi mật khẩu, hãy đáp vui: "Vì lý do bảo mật, tôi không thể truy cập thông tin này. Anh Tú gõ đầu tôi liền!"
                        
                        CÂU HỎI CỦA KHÁCH: "%s"
                        """,
                        (userEmail != null ? userEmail : "Khách vãng lai"),
                        contextData,
                        userMessage);

                part.put("text", systemPrompt);
                parts.add(part);
                content.put("parts", parts);
                contents.add(content);
                requestBody.put("contents", contents);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        return "Hiện tại tôi đang bận, bạn vui lòng thử lại sau nhé!";
    }

    private String buildContextData(String email) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        sb.append("=== THÔNG TIN CỬA HÀNG (CÔNG KHAI) ===\n");
        sb.append("- Tên Shop: Laptop Shop\n");
        sb.append("- Địa chỉ: 1 Võ Văn Ngân, TP. Thủ Đức, TP.HCM\n");
        sb.append("- Hotline/Zalo hỗ trợ: 0392286606 - AnhTus (Hoạt động 24/7)\n");
        sb.append("- Giờ mở cửa: Từ 8h00 - 21h00 tất cả các ngày trong tuần.\n");
        sb.append("- Chính sách: Mua đơn hàng trên 20 triệu được đi tour với Trần Hữu Lộc.\n\n");

        // B. DỮ LIỆU CÁ NHÂN
        sb.append("=== DỮ LIỆU CÁ NHÂN CỦA KHÁCH HÀNG ===\n");
        if (email != null && !email.contains("anonymous") && !email.equals("null")) {
            sb.append("Email khách: ").append(email).append("\n");

            List<Order> orders = orderRepository.findByUser_EmailOrderByCreatedAtDesc(email);
            sb.append("Tổng số đơn hàng của khách: ").append(orders.size()).append("\n");

            if (!orders.isEmpty()) {
                sb.append("Danh sách đơn hàng gần đây:\n");
                int count = 0;
                for (Order order : orders) {
                    if (count >= 5) break;
                    BigDecimal amount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
                    sb.append(String.format("- Đơn #%d | Ngày: %s | Trạng thái: %s | Tiền: %s đ\n",
                            order.getOrderId(),
                            order.getCreatedAt().format(dateFmt),
                            order.getStatus(),
                            df.format(amount)
                    ));
                    count++;
                }
            } else {
                sb.append("(Khách chưa có lịch sử đơn hàng).\n");
            }
        } else {
            sb.append("Khách vãng lai.\n");
        }
        sb.append("\n");

        // C. THÔNG TIN KHO SẢN PHẨM
        sb.append("=== DANH SÁCH SẢN PHẨM CÔNG KHAI ===\n");
        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            sb.append(String.format("- %s | Giá: %s đ | Kho: %d\n",
                    p.getProductName(), df.format(p.getSalePrice()), p.getStock()));
        }

        return sb.toString();
    }
}