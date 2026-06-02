package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.WarrantyPolicy;
import com.example.laptopshop.service.WarrantyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/warranty-policies")
@RequiredArgsConstructor
public class AdminWarrantyPolicyController {

    private final WarrantyPolicyService warrantyPolicyService;

    // 1. Danh sách
    @GetMapping
    public String index(Model model) {
        List<WarrantyPolicy> policies = warrantyPolicyService.getAll();
        model.addAttribute("policies", policies);
        model.addAttribute("activePage", "warranty-policies");
        return "admin/warranty_policy/list";
    }

    // 2. Form thêm mới
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("policy", new WarrantyPolicy());
        model.addAttribute("activePage", "warranty-policies");
        return "admin/warranty_policy/form";
    }

    // 3. Form cập nhật
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        WarrantyPolicy policy = warrantyPolicyService.getById(id);
        if (policy == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chính sách bảo hành!");
            return "redirect:/admin/warranty-policies";
        }
        model.addAttribute("policy", policy);
        model.addAttribute("activePage", "warranty-policies");
        return "admin/warranty_policy/form";
    }

    // 4. Lưu dữ liệu (Dùng chung cho Create & Edit)
    @PostMapping("/save")
    public String save(@ModelAttribute("policy") WarrantyPolicy policy, RedirectAttributes redirectAttributes) {
        warrantyPolicyService.save(policy);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu chính sách bảo hành thành công!");
        return "redirect:/admin/warranty-policies";
    }

    // 5. Xóa dữ liệu
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            warrantyPolicyService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chính sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa! Chính sách này đang được áp dụng cho một số sản phẩm.");
        }
        return "redirect:/admin/warranty-policies";
    }
}