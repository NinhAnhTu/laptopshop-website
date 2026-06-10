package com.example.laptopshop.controller.admin;

import com.example.laptopshop.dto.request.ImportReceiptDTO;
import com.example.laptopshop.entity.ImportReceipt;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.BrandRepository;
import com.example.laptopshop.repository.CategoryRepository;
import com.example.laptopshop.repository.SupplierRepository;
import com.example.laptopshop.service.ImportReceiptService;
import com.example.laptopshop.service.ProductService;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class AdminImportController {

    private final ImportReceiptService importReceiptService;
    private final SupplierRepository supplierRepository;
    private final ProductService productService;
    private final UserService userService;

    // 1. TRANG DANH SÁCH PHIẾU NHẬP
    @GetMapping
    public String listReceipts(Model model,
                               @RequestParam(required = false) Long supplierId,
                               @RequestParam(required = false) String productName,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               @RequestParam(defaultValue = "1") int page) {

        int pageSize = 10; // Cài đặt hiển thị 10 phiếu nhập 1 trang
        Page<ImportReceipt> receiptPage = importReceiptService.searchReceipts(supplierId, productName, startDate, endDate, page, pageSize);

        model.addAttribute("receipts", receiptPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", receiptPage.getTotalPages());
        model.addAttribute("totalItems", receiptPage.getTotalElements());

        // Trả lại các giá trị filter về View để giữ form không bị reset khi load trang
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("productName", productName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Load danh sách nhà cung cấp cho Dropdown
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "admin/import/list";
    }

    // 2. TRANG CHI TIẾT 1 PHIẾU NHẬP
    @GetMapping("/detail/{id}")
    public String viewReceiptDetail(@PathVariable Long id, Model model) {
        model.addAttribute("receipt", importReceiptService.getReceiptById(id));
        return "admin/import/detail";
    }

    // 3. HIỂN THỊ FORM TẠO PHIẾU NHẬP MỚI
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("importDTO", new ImportReceiptDTO());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("products", productService.getAllProducts()); // Load tất cả sản phẩm để chọn nhập
        return "admin/import/create";
    }

    // 4. XỬ LÝ LƯU PHIẾU NHẬP
    @PostMapping("/create")
    public String createImportReceipt(@ModelAttribute("importDTO") ImportReceiptDTO dto,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng đang đăng nhập hệ thống
            String email = principal.getName();
            User currentUser = userService.findByEmail(email);

            // Gọi service xử lý nghiệp vụ phức tạp
            importReceiptService.saveImportReceipt(dto, currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công! Tồn kho và mã Serial đã được tự động cập nhật.");
            return "redirect:/admin/import";

        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào (Trùng serial, lệch số lượng...), hệ thống tự rollback và báo lỗi ra màn hình
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập kho: " + e.getMessage());
            return "redirect:/admin/import/create";
        }
    }
    @ModelAttribute("activePage")
    public String activePage() {
        return "import";
    }
}