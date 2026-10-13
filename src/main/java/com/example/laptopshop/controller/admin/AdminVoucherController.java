package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Voucher;
import com.example.laptopshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       Model model) {

        // Gọi hàm search thay vì getAllVouchers()
        model.addAttribute("vouchers", voucherService.searchVouchers(keyword, status));

        // Trả lại các giá trị này ra View để Form giữ nguyên chữ vừa gõ/trạng thái vừa chọn
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        model.addAttribute("activePage", "vouchers");
        return "admin/voucher/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("activePage", "vouchers");
        return "admin/voucher/create";
    }

    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute("voucher") Voucher voucher, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Validate Ngày tháng (Giữ nguyên logic cũ nhưng đổi cách trả về)
            if (voucher.getStartDate() != null && voucher.getEndDate() != null) {
                if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
                    model.addAttribute("errorMessage", "Ngày kết thúc phải SAU ngày bắt đầu!");
                    model.addAttribute("voucher", voucher);
                    return voucher.getVoucherId() != null ? "admin/voucher/edit" : "admin/voucher/create";
                }
            }

            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công!");
            return "redirect:/admin/vouchers";

        } catch (DataIntegrityViolationException e) {
            // BẮT LỖI TRÙNG MÃ CODE
            model.addAttribute("errorMessage", "Lỗi: Mã Voucher này đã tồn tại!");
            model.addAttribute("voucher", voucher);
            return voucher.getVoucherId() != null ? "admin/voucher/edit" : "admin/voucher/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("voucher", voucher);
            return voucher.getVoucherId() != null ? "admin/voucher/edit" : "admin/voucher/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getById(id);
        if (voucher == null) return "redirect:/admin/vouchers";

        model.addAttribute("voucher", voucher);
        model.addAttribute("activePage", "vouchers");
        return "admin/voucher/edit";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa vì voucher này đã có đơn hàng sử dụng!");
        }
        return "redirect:/admin/vouchers";
    }
}