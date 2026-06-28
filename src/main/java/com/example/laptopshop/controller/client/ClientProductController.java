package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.OrderDetail;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.Review;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.SpecCpuRepository;
import com.example.laptopshop.repository.SpecRamRepository;
import com.example.laptopshop.repository.SpecStorageRepository;
import com.example.laptopshop.repository.SpecVgaRepository;
import com.example.laptopshop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // [QUAN TRỌNG: Import thêm dòng này]
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
    private final OrderService orderService;

    private final SpecRamRepository specRamRepository;
    private final SpecStorageRepository specStorageRepository;
    private final SpecCpuRepository specCpuRepository;
    private final SpecVgaRepository specVgaRepository;

    // [HÀM MỚI] Xử lý lấy User an toàn cho cả Google Login và Đăng nhập thường
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        if (principal instanceof OAuth2AuthenticationToken) {
            email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
        }
        return userService.findByEmail(email);
    }

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
                                    Principal principal,
                                    Model model) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return "redirect:/";
        }

        // [MỚI] Truy vấn linh kiện bằng Product ID (Không cần biết là Category gì, có dữ liệu thì nạp)
        specRamRepository.findById(product.getProductId()).ifPresent(ram -> model.addAttribute("specRam", ram));
        specStorageRepository.findById(product.getProductId()).ifPresent(storage -> model.addAttribute("specStorage", storage));
        specCpuRepository.findById(product.getProductId()).ifPresent(cpu -> model.addAttribute("specCpu", cpu));
        specVgaRepository.findById(product.getProductId()).ifPresent(vga -> model.addAttribute("specVga", vga));

        // Logic check điều kiện khóa/mở Form Đánh giá
        boolean canReview = false;
        User user = getUserFromPrincipal(principal);

        if (user != null) {
            List<Order> orders = orderService.getOrdersByUser(user);
            if (orders != null) {
                for (Order order : orders) {
                    if ("Đã giao".equals(order.getStatus()) || "Đã thanh toán".equals(order.getStatus())) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            if (detail.getProduct().getProductId().equals(product.getProductId())) {
                                canReview = true;
                                break;
                            }
                        }
                    }
                    if (canReview) break;
                }
            }
        }

        model.addAttribute("canReview", canReview);

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

        // [ĐÃ SỬA] Dùng hàm lấy User an toàn
        User user = getUserFromPrincipal(principal);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            reviewService.saveReview(user, productId, comment, rating);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        Product product = productService.getProductById(productId);
        return "redirect:/product/" + product.getSlug() + "#reviews";
    }
}