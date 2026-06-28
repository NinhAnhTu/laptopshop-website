package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.RecommendationService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional; // [THÊM IMPORT NÀY]
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductRepository productRepository;
    private final RecommendationService recommendationService;
    private final UserService userService;

    // 1. API Trả về kết quả Live Search
    @GetMapping("/search-suggestions")
    @Transactional(readOnly = true) // [QUAN TRỌNG]: Giúp giữ kết nối DB để lấy được danh sách Ảnh
    public ResponseEntity<List<Map<String, Object>>> getSearchSuggestions(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Lấy top 5 sản phẩm khớp từ khóa
        List<Product> products = productRepository.findTop5ByProductNameContainingIgnoreCase(keyword.trim());
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", p.getProductName());
            item.put("salePrice", p.getSalePrice());
            item.put("slug", p.getSlug());

            // Lấy URL ảnh Thumbnail
            String imageUrl = "https://via.placeholder.com/50";
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                imageUrl = p.getImages().get(0).getUrl();
            }
            item.put("imageUrl", imageUrl);
            resultList.add(item);
        }
        return ResponseEntity.ok(resultList);
    }

    // 2. API Lưu lịch sử tìm kiếm (Hỗ trợ hệ thống gợi ý)
    @PostMapping("/record-click")
    public ResponseEntity<Void> recordSearchClick(@RequestParam("keyword") String keyword, Principal principal) {
        if (principal != null && keyword != null && !keyword.trim().isEmpty()) {
            // Lấy User hiện tại
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
            }
            User user = userService.findByEmail(email);

            // Lưu từ khóa vào lịch sử để làm Recommender System
            if (user != null) {
                recommendationService.saveSearch(user, keyword);
            }
        }
        return ResponseEntity.ok().build();
    }
}