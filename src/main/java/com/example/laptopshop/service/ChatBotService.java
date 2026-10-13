package com.example.laptopshop.service;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.repository.OrderRepository;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor
public class ChatBotService {

    @Value("${gemini.api.key.primary}")
    private String primaryKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key.fallback_1:}") // Dấu ":" giúp code không lỗi nếu bạn xóa dòng này trong file properties
    private String fallbackKey1;

    @Value("${gemini.api.key.fallback_2:}")
    private String fallbackKey2;

    private final ProductService productService;
    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ChatBotService.class);

    public String getAutoReply(String userMessage, String userEmail, String conversationSummary) {
        String contextData = buildContextData(userEmail);
        String summary = (conversationSummary != null) ? conversationSummary : "Chưa có";

        // [ĐÃ NÂNG CẤP PROMPT] Dạy AI cách tư vấn theo giá, máy ngon nhất và tự động gọi Admin
        String systemPrompt = String.format("""
                Vai trò: Bạn là Trợ lý AI tư vấn bán hàng thông minh của "Laptop Shop ChaosCoders".
                Khách hàng đang chat: %s
                
                TÓM TẮT LỊCH SỬ CHAT TRƯỚC ĐÓ: %s
                
                DỮ LIỆU CUNG CẤP TỪ HỆ THỐNG:
                %s
                
                HƯỚNG DẪN TƯ VẤN CỰC KỲ QUAN TRỌNG:
               1. LUẬT NGỮ CẢNH: Khi khách dùng các từ "nó", "cái đó", "máy đó","sản phẩm đó", "sản phẩm vừa rồi", "sản phẩm trên", bạn BẮT BUỘC phải đọc "LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY" để nhận diện chính xác tên máy khách đang nói tới.
               2. GẮN THẺ SẢN PHẨM: Khi gợi ý 1 laptop, BẮT BUỘC chèn mã [PRODUCT:id] vào cuối câu (VD: [PRODUCT:1]).
               3. TÌM MÁY THEO GIÁ/NHU CẦU: Tự tìm trong DANH SÁCH SẢN PHẨM KHẢ DỤNG chiếc máy phù hợp nhất với giá tiền và nhu cầu của khách.
               4. CHÍNH SÁCH BẢO HÀNH: Nếu khách hỏi bảo hành, hãy trả lời mặc định là: "Tất cả các máy tại Laptop Shop đều được bảo hành chính hãng 12 tháng và đổi trả 1-1 trong 7 ngày đầu nếu có lỗi phần cứng".
               5. KẾT NỐI ADMIN: Nếu khách cáu gắt, đòi gặp người thật, hoặc hỏi ngoài lề (hỏi mua điện thoại, tủ lạnh), hoặc bạn KHÔNG TÌM THẤY thông tin, trả lời đúng 1 từ: CONNECT_ADMIN
               6. HỎI TỪNG BƯỚC: TUYỆT ĐỐI KHÔNG hỏi dồn dập 2-3 câu cùng lúc. Hãy hỏi từng thông tin một (VD: Bạn cần máy để làm gì?). Đợi khách trả lời xong mới hỏi tiếp (VD: Ngân sách của bạn bao nhiêu?).
               7. LÀM RÕ THÔNG TIN: Nếu khách trả lời quá ngắn hoặc chưa đủ rõ ràng để tư vấn (VD: Khách chỉ nói "Tôi cần mua máy"), hãy nhẹ nhàng hỏi thêm để thu thập đủ dữ kiện trước khi gợi ý máy.
                CÂU HỎI CỦA KHÁCH: "%s"
                """,
                (userEmail != null ? userEmail : "Khách vãng lai"),
                summary,
                contextData,
                userMessage);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", systemPrompt)))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Gom các key hợp lệ (không null, không rỗng) vào một danh sách
        List<String> activeKeys = java.util.stream.Stream.of(primaryKey, fallbackKey1, fallbackKey2)
                .filter(key -> key != null && !key.trim().isEmpty())
                .toList();

        // Luân phiên chạy API qua danh sách Key
        for (int i = 0; i < activeKeys.size(); i++) {
            String currentKey = activeKeys.get(i).trim();
            String finalUrl = apiUrl + "?key=" + currentKey;

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Lỗi 4xx (429 Hết Quota, 403 Sai Key, 400 Bad Request...)
                String keyName = (i == 0) ? "PRIMARY" : "FALLBACK_" + i;
                log.warn("❌ Lỗi API Key [{}] ({}) -> Status: {}", keyName, maskKey(currentKey), e.getStatusCode());

                if (i == activeKeys.size() - 1) {
                    log.error("💀 TẤT CẢ CÁC API KEY ĐỀU ĐÃ HẾT QUOTA HOẶC BỊ LỖI!");
                } else {
                    log.info("🔄 Đang tự động chuyển sang thử nghiệm API Key tiếp theo...");
                }
            } catch (Exception e) {
                // Các lỗi khác (Mất mạng, Time Out...)
                log.error("⚠️ Lỗi không mong muốn khi gọi API: {}", e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        return "Hiện tại hệ thống AI đang quá tải, bạn vui lòng chờ giây lát để nhân viên hỗ trợ nhé!";
    }

    // Hàm ẩn giấu một phần API Key khi ghi ra Log để bảo mật (Để hàm này bên dưới getAutoReply)
    private String maskKey(String key) {
        if (key == null || key.length() < 10) return "INVALID_KEY";
        return key.substring(0, 5) + "..." + key.substring(key.length() - 5);
    }

    private String buildContextData(String email) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        sb.append("=== THÔNG TIN CỬA HÀNG ===\n");
        sb.append("- Địa chỉ: 1 Võ Văn Ngân, TP. Thủ Đức, TP.HCM\n");
        sb.append("- Hotline: 0392286606 - AnhTus\n");
        sb.append("- Chính sách: Mua đơn trên 20 triệu được đi tour với Trần Hữu Lộc.\n\n");

        if (email != null && !email.contains("anonymous") && !email.equals("null")) {
            sb.append("=== LỊCH SỬ ĐƠN HÀNG CỦA KHÁCH ===\n");
            List<Order> orders = orderRepository.findByUser_EmailOrderByCreatedAtDesc(email);
            int count = 0;
            for (Order order : orders) {
                if (count >= 5) break;
                BigDecimal amount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
                sb.append(String.format("- Đơn #%d | Trạng thái: %s | Tiền: %s đ\n", order.getOrderId(), order.getStatus(), df.format(amount)));
                count++;
            }
            sb.append("\n");
        }

        sb.append("=== DANH SÁCH SẢN PHẨM KHẢ DỤNG ===\n");
        List<Product> products = productService.getAllProducts();

        List<Product> activeProducts = products.stream()
                .filter(Product::getIsActive)
                .toList();

        if (!activeProducts.isEmpty()) {
            // [MỚI] Tự động tính toán các máy nổi bật bằng Java
            Product mostExpensive = activeProducts.stream()
                    .max(java.util.Comparator.comparing(Product::getSalePrice))
                    .orElse(activeProducts.get(0));

            Product cheapest = activeProducts.stream()
                    .min(java.util.Comparator.comparing(Product::getSalePrice))
                    .orElse(activeProducts.get(0));

            // Tìm 1 máy "Quốc Dân" (Lấy đại 1 máy tầm trung trong mảng làm Best Choice)
            Product bestChoice = activeProducts.get(activeProducts.size() / 2);

            // Truyền Gợi ý đặc biệt vào cho AI
            sb.append("👉 GỢI Ý ĐẶC BIỆT:\n");
            sb.append(String.format("- MÁY ĐẮT NHẤT: ID: %d | Tên: %s | Giá: %s đ\n",
                    mostExpensive.getProductId(), mostExpensive.getProductName(), df.format(mostExpensive.getSalePrice())));
            sb.append(String.format("- MÁY RẺ NHẤT: ID: %d | Tên: %s | Giá: %s đ\n",
                    cheapest.getProductId(), cheapest.getProductName(), df.format(cheapest.getSalePrice())));
            sb.append(String.format("- MÁY QUỐC DÂN (Ngon bổ rẻ/OK Nhất): ID: %d | Tên: %s | Giá: %s đ\n\n",
                    bestChoice.getProductId(), bestChoice.getProductName(), df.format(bestChoice.getSalePrice())));

            sb.append("Danh sách các máy khác để tra cứu giá:\n");
            int count = 0;
            for (Product p : activeProducts) {
                if (count >= 15) break; // Tăng lên 15 máy để AI có nhiều lựa chọn về mức giá
                sb.append(String.format("- ID: %d | Tên: %s | Giá: %s đ\n",
                        p.getProductId(), p.getProductName(), df.format(p.getSalePrice())));
                count++;
            }
        }

        return sb.toString();
    }
}