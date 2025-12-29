package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Review;
import com.example.laptopshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    // --- CHỈ GIỮ LẠI HÀM NÀY ---
    @GetMapping
    public String listReviewss(Model model,
                               @RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "rating", required = false) Integer rating,
                               @RequestParam(name = "status", required = false) String status) {

        // Lấy dữ liệu có lọc từ Service
        List<Review> reviews = reviewService.getAllReviewss(keyword, rating, status);

        // Gửi dữ liệu sang HTML với tên biến là "listReviewss"
        model.addAttribute("listReviewss", reviews);

        // Gửi lại các từ khóa để giữ trên ô input
        model.addAttribute("keyword", keyword);
        model.addAttribute("rating", rating);
        model.addAttribute("status", status);

        return "admin/review/list";
    }

    // 2. Xử lý trả lời (Reply)
    @PostMapping("/reply")
    public String replyReview(@RequestParam("id") Long id,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.replyToReview(id, content);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi phản hồi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // 3. Xử lý xóa (Delete)
    @PostMapping("/delete")
    public String deleteReview(@RequestParam("id") Long id,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá và gửi mail thông báo!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}