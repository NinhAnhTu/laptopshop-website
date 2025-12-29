package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.service.ProductSerialService;
import com.example.laptopshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final ProductService productService;
    private final ProductSerialService productSerialService;

    // 1. Danh sách sản phẩm để quản lý kho
    @GetMapping
    public String inventoryList(Model model,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {

        // Gọi hàm tìm kiếm kho
        // Nếu không nhập gì, nó sẽ trả về tất cả sản phẩm
        List<Product> products = productService.searchInventory(keyword, status);

        model.addAttribute("products", products);

        // Giữ lại giá trị filter để hiển thị lại trên form sau khi reload trang
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        model.addAttribute("activePage", "inventory");
        return "admin/inventory/list";
    }

    // 2. Chi tiết kho của 1 sản phẩm (Xem serials + Nhập thêm)
    @GetMapping("/{productId}")
    public String viewProductSerials(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.getProductById(productId));
        model.addAttribute("serials", productSerialService.getSerialsByProductId(productId));
        model.addAttribute("activePage", "inventory");
        return "admin/inventory/details";
    }

    // 3. Xử lý nhập kho (Thêm Serial)
    @PostMapping("/import")
    public String importSerials(@RequestParam("productId") Long productId,
                                @RequestParam("serialList") String serialList,
                                RedirectAttributes redirectAttributes) {
        try {
            productSerialService.importSerials(productId, serialList);
            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory/" + productId;
    }

    // 4. Xóa Serial (Điều chỉnh kho)
    @GetMapping("/delete-serial/{id}")
    public String deleteSerial(@PathVariable Long id, @RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
        try {
            productSerialService.deleteSerial(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa Serial thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/inventory/" + productId;
    }
}