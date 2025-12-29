package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Warranty;
import com.example.laptopshop.service.WarrantyService;
import com.example.laptopshop.repository.WarrantyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/warranties")
@RequiredArgsConstructor
public class AdminWarrantyController {

    private final WarrantyService warrantyService;
    private final WarrantyRepository warrantyRepository;

    // 1. Hiển thị danh sách
    @GetMapping
    public String index(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Warranty> list;
        if (keyword != null && !keyword.isEmpty()) {
            list = warrantyService.search(keyword);
        } else {
            list = warrantyService.getAll();
        }
        model.addAttribute("warranties", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "warranties");
        return "admin/warranty/list";
    }

    // 2. Hiển thị form Edit
    @GetMapping("/edit/{id}")
    public String editWarranty(@PathVariable Long id, Model model) {
        Warranty warranty = warrantyRepository.findById(id).orElse(null);
        if (warranty == null) {
            return "redirect:/admin/warranties";
        }
        model.addAttribute("warranty", warranty);
        model.addAttribute("activePage", "warranties");
        return "admin/warranty/edit";
    }

    // 3. Xử lý lưu (Cập nhật)
    @PostMapping("/save")
    public String saveWarranty(@ModelAttribute("warranty") Warranty formWarranty, RedirectAttributes redirectAttributes) {
        Warranty dbWarranty = warrantyRepository.findById(formWarranty.getId()).orElse(null);

        if (dbWarranty != null) {
            dbWarranty.setExpirationDate(formWarranty.getExpirationDate());
            dbWarranty.setStatus(formWarranty.getStatus());

            warrantyRepository.save(dbWarranty);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu bảo hành thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu bảo hành!");
        }

        return "redirect:/admin/warranties";
    }

    @GetMapping("/scan")
    public String scanExpiringWarranties(RedirectAttributes redirectAttributes) {
        try {
            // 1. Gửi mail nhắc nhở cho những người SẮP hết hạn (Tương lai)
            warrantyService.sendWarrantyExpiryReminders();

            // 2. Cập nhật trạng thái cho những người ĐÃ hết hạn (Quá khứ)
            warrantyService.scanAndExpireWarranties();

            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi mail nhắc nhở và cập nhật trạng thái!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/warranties";
    }
}