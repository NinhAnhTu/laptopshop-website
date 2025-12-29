package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.Review;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.BrandService;
import com.example.laptopshop.service.CategoryService;
import com.example.laptopshop.service.ProductService;
import com.example.laptopshop.service.ReviewService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientProductController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    // --- 1. TRANG CỬA HÀNG ---
    @GetMapping("/store")
    public String shopPage(Model model,
                           @RequestParam(required = false) Long brand,
                           @RequestParam(required = false) Long category,
                           @RequestParam(required = false) String price,
                           @RequestParam(required = false) Integer rating) {

        List<Product> products = productService.filterProducts(category, brand, price, rating);
        model.addAttribute("products", products);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());

        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPrice", price);
        model.addAttribute("selectedRating", rating);

        return "client/shop";
    }

    // --- 2. CHI TIẾT SẢN PHẨM ---
    @GetMapping("/product/{slug}")
    public String showProductDetail(@PathVariable String slug,
                                    @RequestParam(defaultValue = "1") int page,
                                    Model model) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return "redirect:/";
        }

        // Cấu hình số lượng review mỗi trang
        int pageSize = 3;

        Page<Review> reviewPage = reviewService.getReviewsByProductIds(product.getProductId(), page, pageSize);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());

        return "client/product_detail";
    }

    // --- 3. XỬ LÝ GỬI ĐÁNH GIÁ ---
    @PostMapping("/review/add")
    public String addReview(@RequestParam("productId") Long productId,
                            @RequestParam("comment") String comment,
                            @RequestParam("rating") int rating,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String email = principal.getName();
            User user = userService.findByEmail(email);

            reviewService.saveReview(user, productId, comment, rating);

            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        Product product = productService.getProductById(productId);
        return "redirect:/product/" + product.getSlug() + "#reviews";
    }
}