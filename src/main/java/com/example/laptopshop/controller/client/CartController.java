package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Cart;
import com.example.laptopshop.entity.CartDetail;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    // 1. Hiển thị trang giỏ hàng
    @GetMapping
    public String showCart(Model model, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(user);

        // Tính tổng tiền tạm tính
        double totalPrice = 0;
        if (cart != null && cart.getCartDetails() != null) {
            totalPrice = cart.getCartDetails().stream()
                    .mapToDouble(item -> item.getProduct().getSalePrice().doubleValue() * item.getQuantity())
                    .sum();
        }

        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", totalPrice);
        return "client/cart";
    }

    // 2. Thêm vào giỏ
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            Authentication authentication) {

        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        cartService.addToCart(user, productId, quantity);

        return "redirect:/cart";
    }

    // 3. Xóa sản phẩm
    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long cartDetailId) {
        cartService.removeFromCart(cartDetailId);
        return "redirect:/cart";
    }

    // 4. Cập nhật số lượng
    @PostMapping("/update")
    public String updateQuantity(@RequestParam("id") Long cartDetailId,
                                 @RequestParam("quantity") int quantity,
                                 RedirectAttributes redirectAttributes) { // [MỚI] Thêm RedirectAttributes để gửi thông báo
        try {
            // [YÊU CẦU 2] Nếu số lượng <= 0 thì xóa khỏi giỏ
            if (quantity <= 0) {
                cartService.removeFromCart(cartDetailId);
            } else {
                // [YÊU CẦU 3] Cập nhật (Logic check tồn kho nằm trong Service)
                cartService.updateQuantity(cartDetailId, quantity);
            }
        } catch (Exception e) {
            // Bắt lỗi từ Service (Vượt quá tồn kho) và gửi ra giao diện
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    // lấy User chuẩn từ cả Google và Login thường
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = "";
        // Trường hợp 1: Đăng nhập bằng Google (OAuth2)
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            email = oauthToken.getPrincipal().getAttribute("email");
        }
        // Trường hợp 2: Đăng nhập thường (Database)
        else {
            email = authentication.getName();
        }

        // Tìm User trong DB
        if (email != null && !email.isEmpty()) {
            return userService.findByEmail(email);
        }
        return null;
    }

    //Thêm nhiều sản phẩm (dành cho PC Builder)
    @PostMapping("/add-multiple")
    @ResponseBody
    public String addMultipleToCart(@RequestBody List<Map<String, Integer>> items, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return "redirect:/login"; // Cần xử lý redirect ở frontend nếu nhận response này
        }

        try {
            for (Map<String, Integer> item : items) {
                Long productId = Long.valueOf(item.get("productId"));
                Integer quantity = item.get("quantity");
                cartService.addToCart(user, productId, quantity);
            }
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/clear")
    public String clearCart(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(user);
        if (cart != null && cart.getCartDetails() != null) {
            // Lấy danh sách ID trước để tránh lỗi ConcurrentModificationException
            List<Long> detailIds = cart.getCartDetails().stream()
                    .map(CartDetail::getCartDetailId)
                    .toList();

            for (Long id : detailIds) {
                cartService.removeFromCart(id);
            }
        }
        return "redirect:/cart";
    }
}