package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Voucher;
import com.example.laptopshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
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
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        model.addAttribute("activePage", "vouchers"); // Để active menu sidebar
        return "admin/voucher/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("activePage", "vouchers");
        return "admin/voucher/create";
    }

    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute("voucher") Voucher voucher, RedirectAttributes redirectAttributes) {
        try {
            // Validate Ngày tháng
            if (voucher.getStartDate() != null && voucher.getEndDate() != null) {
                if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Ngày kết thúc phải SAU ngày bắt đầu!");
                    redirectAttributes.addFlashAttribute("voucher", voucher); // Giữ lại dữ liệu cũ để đỡ nhập lại

                    if (voucher.getVoucherId() != null) {
                        return "redirect:/admin/vouchers/edit/" + voucher.getVoucherId();
                    } else {
                        return "redirect:/admin/vouchers/create";
                    }
                }
            }

            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucher);

            if (voucher.getVoucherId() != null) {
                return "redirect:/admin/vouchers/edit/" + voucher.getVoucherId();
            } else {
                return "redirect:/admin/vouchers/create";
            }
        }
        return "redirect:/admin/vouchers";
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