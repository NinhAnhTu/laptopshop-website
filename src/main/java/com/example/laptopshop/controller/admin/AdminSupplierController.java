package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Supplier;
import com.example.laptopshop.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {

    private final SupplierService supplierService;

    // 1. Danh sách
    @GetMapping
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("activePage", "suppliers");
        return "admin/supplier/list";
    }

    // 2. Form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("activePage", "suppliers");
        return "admin/supplier/create";
    }

    // 3. Xử lý lưu
    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute("supplier") Supplier supplier, RedirectAttributes redirectAttributes) {
        supplierService.saveSupplier(supplier);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu nhà cung cấp thành công!");
        return "redirect:/admin/suppliers";
    }

    // 4. Form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier == null) {
            return "redirect:/admin/suppliers";
        }
        model.addAttribute("supplier", supplier);
        model.addAttribute("activePage", "suppliers");
        return "admin/supplier/edit";
    }

    // 5. Xử lý xóa
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa vì nhà cung cấp này đang có sản phẩm liên kết!");
        }
        return "redirect:/admin/suppliers";
    }
}