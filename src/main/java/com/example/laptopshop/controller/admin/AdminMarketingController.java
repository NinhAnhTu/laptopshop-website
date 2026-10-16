package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.UserRepository;
import com.example.laptopshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/marketing")
@RequiredArgsConstructor
public class AdminMarketingController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Hiển thị giao diện gửi mail
    @GetMapping("/campaigns")
    public String showCampaignForm(Model model) {
        // Chỉ lấy những tài khoản có vai trò Customer (userTypeId = 2)
        List<User> customers = userRepository.findByUserType_UserTypeId(2L);
        long customerCount = customers != null ? customers.size() : 0;

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("activePage", "marketing");
        return "admin/marketing_campaign";
    }

    // Xử lý logic gửi mail
    @PostMapping("/send-email")
    public String sendEmailToCustomers(
            @RequestParam("subject") String subject,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {

        try {
            // Lấy danh sách user là Customer
            List<User> customers = userRepository.findByUserType_UserTypeId(2L);

            // Lọc lấy danh sách email hợp lệ
            List<String> customerEmails = customers.stream()
                    .filter(u -> u.getEmail() != null && !u.getEmail().trim().isEmpty() && u.getEmail().contains("@"))
                    .map(User::getEmail)
                    .collect(Collectors.toList());

            if (customerEmails.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không có khách hàng nào có địa chỉ email hợp lệ!");
                return "redirect:/admin/marketing/campaigns";
            }

            // Gọi EmailService gửi hàng loạt (chạy bất đồng bộ)
            emailService.sendBulkMarketingEmail(customerEmails, subject, content);

            redirectAttributes.addFlashAttribute("successMessage", "Chiến dịch email đang được xử lý gửi đến " + customerEmails.size() + " khách hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/marketing/campaigns";
    }
}