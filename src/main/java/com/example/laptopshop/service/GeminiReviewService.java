package com.example.laptopshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiReviewService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateReply(String userName, String productName, String comment) {
        String finalUrl = apiUrl + "?key=" + apiKey;

        String prompt = String.format(
                "Bạn là nhân viên Chăm sóc khách hàng của 'Laptop Shop ChaosCoders'. " +
                        "Khách hàng tên \"%s\" vừa đánh giá 5 sao cho sản phẩm \"%s\" với nội dung: \"%s\". " +
                        "Hãy viết một câu trả lời ngắn gọn (dưới 50 từ), giọng văn thân thiện, biết ơn và chuyên nghiệp bằng tiếng Việt. " +
                        "Chỉ xuất ra nội dung câu trả lời, không thêm lời dẫn.",
                userName, productName, comment
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();

            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Gọi API
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);

            // Parse kết quả
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Trả về null để biết là AI thất bại
        }
    }
}