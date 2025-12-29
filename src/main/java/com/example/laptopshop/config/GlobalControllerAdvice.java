package com.example.laptopshop.config;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;
    private final CartService cartService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Mặc định giỏ hàng bằng 0
        int count = 0;

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = "";
            if (auth instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) auth).getPrincipal().getAttribute("email");
            } else {
                email = auth.getName();
            }

            if (email != null && !email.isEmpty()) {
                User user = userService.findByEmail(email);
                if (user != null) {
                    model.addAttribute("currentUser", user);
                    //Tính số lượng giỏ hàng
                    count = cartService.countItemsInCart(user);
                }
            }
        }
        // Truyền biến cartCount ra cho header.html dùng
        model.addAttribute("cartCount", count);
    }
}