package com.example.laptopshop.service;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.SearchHistory;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final ProductRepository productRepository;

    public void saveSearch(User user, String keyword) {
        if (user != null && keyword != null && !keyword.trim().isEmpty()) {
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setKeyword(keyword.trim());
            history.setSearchTime(LocalDateTime.now());
            searchHistoryRepository.save(history);
        }
    }

    public List<Product> getRecommendedProducts(User user) {
        Set<Product> recommendedProducts = new java.util.LinkedHashSet<>();

        if (user != null) {
            List<String> keywords = searchHistoryRepository.findRecentKeywords(user.getUserId());

            int limit = Math.min(keywords.size(), 5);

            for (int i = 0; i < limit; i++) {
                String key = keywords.get(i);
                List<Product> products = productRepository.findByProductNameContainingIgnoreCase(key, PageRequest.of(0, 4)).getContent();

                for (Product p : products) {
                    if (recommendedProducts.size() >= 4) break;
                    recommendedProducts.add(p);
                }
                if (recommendedProducts.size() >= 4) break;
            }
        }

        if (recommendedProducts.size() < 4) {
            List<Product> bestSellers = productRepository.findTopSellingProducts(PageRequest.of(0, 10));
            for (Product p : bestSellers) {
                if (recommendedProducts.size() >= 4) break;
                recommendedProducts.add(p);
            }
        }

        if (recommendedProducts.size() < 4) {
            List<Product> allProducts = productRepository.findAll(PageRequest.of(0, 10)).getContent();
            for (Product p : allProducts) {
                if (recommendedProducts.size() >= 4) break;
                recommendedProducts.add(p);
            }
        }

        return new ArrayList<>(recommendedProducts);
    }
}   