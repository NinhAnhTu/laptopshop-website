package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.ProductService;
import com.example.laptopshop.service.RecommendationService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final UserService userService;
    private final RecommendationService recommendationService;

    @GetMapping("/")
    public String homePage(Model model,
                           @RequestParam(name = "keyword", required = false) String keyword,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           Principal principal) {

        User currentUser = null;
        if (principal != null) {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
            }
            currentUser = userService.findByEmail(email);
        }

        if (keyword != null && !keyword.trim().isEmpty() && currentUser != null) {
            recommendationService.saveSearch(currentUser, keyword);
        }

        if (currentUser != null) {
            List<Product> recommendedProducts = recommendationService.getRecommendedProducts(currentUser);
            if (recommendedProducts != null && !recommendedProducts.isEmpty()) {
                model.addAttribute("recommendedProducts", recommendedProducts);
            }
        }

        List<Product> bestSellers = productService.getTopSellingProducts(4);
        model.addAttribute("bestSellers", bestSellers);

        int pageSize = 8;
        Page<Product> productPage = productService.getAllProducts(keyword, page, pageSize);

        List<Product> products = productPage.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "client/home";
    }
}