package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Supplier;
import com.example.laptopshop.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {

    private static final String VIEW_CREATE = "admin/supplier/create";
    private static final String VIEW_EDIT   = "admin/supplier/edit";
    private static final String ACTIVE_PAGE = "suppliers";

    private final SupplierService supplierService;

    // 1. Danh sách
    @GetMapping
    public String listSuppliers(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("suppliers", supplierService.searchSuppliers(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "admin/supplier/list";
    }

    // 2. Form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("activePage", ACTIVE_PAGE);
        return VIEW_CREATE;
    }

    // 3. Xử lý lưu (dùng chung Create & Edit)
    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute("supplier") Supplier supplier,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // Có supplierId -> đang sửa; không có -> đang thêm. Dùng để quay lại đúng form khi lỗi.
        String errorView = supplier.getSupplierId() != null ? VIEW_EDIT : VIEW_CREATE;

        // Chuẩn hóa tên: bỏ khoảng trắng đầu/cuối và gộp khoảng trắng thừa ở giữa
        if (supplier.getSupplierName() != null) {
            supplier.setSupplierName(supplier.getSupplierName().trim().replaceAll("\\s+", " "));
        }

        try {
            if (supplier.getSupplierName() == null || supplier.getSupplierName().isEmpty()) {
                return backToForm(model, errorView, "Tên nhà cung cấp không được để trống!");
            }

            // Kiểm tra trùng tên (không phân biệt hoa/thường, bỏ qua chính nó khi đang sửa)
            if (supplierService.isSupplierNameExists(supplier.getSupplierName(), supplier.getSupplierId())) {
                return backToForm(model, errorView, "Tên nhà cung cấp đã tồn tại, vui lòng chọn tên khác!");
            }

            supplierService.saveSupplier(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu nhà cung cấp thành công!");
            return "redirect:/admin/suppliers";

        } catch (DataIntegrityViolationException e) {
            return backToForm(model, errorView, "Dữ liệu không hợp lệ hoặc đã tồn tại!");
        } catch (RuntimeException e) {
            // Lỗi nghiệp vụ từ Service (sđt phải 10 số, trùng sđt...) -> giờ cũng hiện dialog ngay trên form
            return backToForm(model, errorView, e.getMessage());
        } catch (Exception e) {
            return backToForm(model, errorView, "Đã xảy ra lỗi hệ thống không xác định!");
        }
    }

    // 4. Form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier == null) {
            return "redirect:/admin/suppliers";
        }
        model.addAttribute("supplier", supplier);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return VIEW_EDIT;
    }

    // 5. Xử lý xóa
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa vì nhà cung cấp này đang có sản phẩm liên kết!");
        }
        return "redirect:/admin/suppliers";
    }

    // Trả lại form kèm thông báo lỗi. "supplier" đã có sẵn trong model nhờ @ModelAttribute
    // nên dữ liệu người dùng vừa nhập được giữ nguyên.
    private String backToForm(Model model, String view, String message) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return view;
    }
}