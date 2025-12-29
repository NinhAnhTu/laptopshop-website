package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // --- 1. DANH SÁCH USER ---
    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(required = false) String keyword, // Nhận từ khóa
                            @RequestParam(required = false) Long roleId) {  // Nhận ID quyền (admin/customer)

        List<User> users = userService.searchUsers(keyword, roleId);

        model.addAttribute("users", users);

        model.addAttribute("roles", userService.getAllUserTypes());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRoleId", roleId);

        model.addAttribute("activePage", "users");
        return "admin/user/list";
    }

    // --- 2. FORM TẠO MỚI ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        // Lấy danh sách Role để admin chọn khi tạo user mới
        model.addAttribute("roles", userService.getAllUserTypes());
        model.addAttribute("activePage", "users");
        return "admin/user/create";
    }

    // --- 3. LƯU USER  ---
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Email có thể đã tồn tại hoặc lỗi hệ thống!");
        }
        return "redirect:/admin/users";
    }

    // --- 4. FORM SỬA ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) return "redirect:/admin/users";

        user.setPassword("");

        model.addAttribute("user", user);
        model.addAttribute("roles", userService.getAllUserTypes());
        model.addAttribute("activePage", "users");
        return "admin/user/edit";
    }

    // --- 5. XÓA USER ---
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng này (Có thể đang có đơn hàng liên kết)!");
        }
        return "redirect:/admin/users";
    }
}