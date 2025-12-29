package com.example.laptopshop.controller;

import com.example.laptopshop.dto.request.RegisterDTO;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 1. Hiển thị trang Login
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // Trỏ đến file templates/auth/login.html
    }

    // 2. Hiển thị trang đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterDTO());
        return "auth/register"; // Trỏ đến file templates/auth/register.html
    }

    // 3. Xử lý đăng ký
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") RegisterDTO registerDTO,
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Validate mật khẩu nhập lại
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Mật khẩu nhập lại không khớp!");
            return "auth/register";
        }

        try {
            // Chuyển từ DTO sang Entity
            User user = new User();
            user.setFullname(registerDTO.getFullname());
            user.setEmail(registerDTO.getEmail());
            user.setPhone(registerDTO.getPhone());
            user.setPassword(registerDTO.getPassword());
            user.setUsername(registerDTO.getEmail());

            userService.registerUser(user);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registerSuccess";
    }
    // 1. Trang nhập Email (Quên mật khẩu)
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot_password";
    }

    // 2. Xử lý gửi yêu cầu
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.generateResetToken(email);
            model.addAttribute("message", "Link đặt lại mật khẩu đã được gửi vào email của bạn!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "auth/forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            User user = userService.getByResetToken(token);

            // Kiểm tra hết hạn (Expiry)
            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "Link đã hết hạn!");
                return "auth/reset_password";
            }

            model.addAttribute("token", token);
            return "auth/reset_password";

        } catch (Exception e) {
            model.addAttribute("error", "Link không hợp lệ!");
            return "auth/reset_password";
        }
    }

    // 4. Xử lý đổi mật khẩu mới
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            User user = userService.getByResetToken(token);

            // Kiểm tra lại lần nữa cho chắc
            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "Link đã hết hạn!");
                return "auth/reset_password";
            }

            userService.updatePassword(user, password);
            return "redirect:/login?resetSuccess";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "auth/reset_password";
        }
    }
}