package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.WarrantyPolicy;
import com.example.laptopshop.service.WarrantyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/warranty-policies")
@RequiredArgsConstructor
public class AdminWarrantyPolicyController {

    private static final String VIEW_CREATE = "admin/warranty_policy/create";
    private static final String VIEW_EDIT   = "admin/warranty_policy/edit";
    private static final String ACTIVE_PAGE = "warranty-policies";

    private final WarrantyPolicyService warrantyPolicyService;

    // 1. Danh sách
    @GetMapping
    public String index(Model model,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer duration,
                        @RequestParam(defaultValue = "1") int page) {

        int pageSize = 10;
        Page<WarrantyPolicy> policyPage = warrantyPolicyService.searchPolicies(
                keyword, duration, PageRequest.of(page - 1, pageSize));

        model.addAttribute("policyPage", policyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("duration", duration);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "admin/warranty_policy/list";
    }

    // 2. Form thêm mới
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("policy", new WarrantyPolicy());
        model.addAttribute("activePage", ACTIVE_PAGE);
        return VIEW_CREATE;
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
        model.addAttribute("activePage", ACTIVE_PAGE);
        return VIEW_EDIT;
    }

    // 4. Lưu dữ liệu (dùng chung cho Create & Edit, có ID = cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute("policy") WarrantyPolicy policy,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        // Có warrantyId -> đang sửa, trả về edit.html; ngược lại create.html
        String errorView = policy.getWarrantyId() != null ? VIEW_EDIT : VIEW_CREATE;

        if (policy.getPolicyName() != null) {
            policy.setPolicyName(policy.getPolicyName().trim());
        }

        String error = null;
        Integer months = policy.getDurationMonths(); // dùng Integer để an toàn dù entity là int hay Integer

        if (policy.getPolicyName() == null || policy.getPolicyName().isEmpty()) {
            error = "Tên chính sách không được để trống!";
        } else if (months == null || months < 1) {
            error = "Thời hạn bảo hành phải từ 1 tháng trở lên!";
        } else if (warrantyPolicyService.isPolicyNameExists(policy.getPolicyName(), policy.getWarrantyId())) {
            error = "Tên chính sách đã tồn tại, vui lòng chọn tên khác!";
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
            model.addAttribute("activePage", ACTIVE_PAGE);
            // "policy" đã nằm sẵn trong model nhờ @ModelAttribute -> form giữ nguyên dữ liệu vừa nhập
            return errorView;
        }

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
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa! Chính sách này đang được áp dụng cho một số sản phẩm.");
        }
        return "redirect:/admin/warranty-policies";
    }
}