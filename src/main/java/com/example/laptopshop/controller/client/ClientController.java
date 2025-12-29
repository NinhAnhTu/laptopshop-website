package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientController {

    private final UserService userService;
    private final OrderService orderService;

    // --- 1. TRANG TÀI KHOẢN ---
    @GetMapping("/account")
    public String myAccount(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        if (principal instanceof OAuth2AuthenticationToken) {
            email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
        }

        User user = userService.findByEmail(email);
        if (user == null) return "redirect:/login";

        List<Order> orders = orderService.getOrdersByUser(user);
        if (orders == null) orders = Collections.emptyList();

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "client/account";
    }

    // --- 2. CẬP NHẬT THÔNG TIN ---
    @PostMapping("/account/update")
    public String updateAccount(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/account";
    }

    // --- 3. HỦY ĐƠN HÀNG ---
    @GetMapping("/account/cancel-order/{id}")
    public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        orderService.cancelOrder(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " thành công!");
        return "redirect:/account";
    }

    // --- 4. ĐỔI MẬT KHẨU ---
    @PostMapping("/account/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/account";
        }

        try {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
            }
            User currentUser = userService.findByEmail(email);

            userService.changeClientPassword(currentUser, oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Kiểm tra email để xem thông tin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/account";
    }
}