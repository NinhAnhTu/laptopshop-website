package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Brand;
import com.example.laptopshop.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.laptopshop.util.UploadService;
@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;
    private final UploadService uploadService;

    @GetMapping
    public String listBrands(@RequestParam(required = false) String keyword, Model model) {

        // Gọi hàm tìm kiếm thay vì lấy tất cả
        model.addAttribute("brands", brandService.searchBrands(keyword));

        // Trả lại keyword về View để ô input giữ lại chữ vừa gõ
        model.addAttribute("keyword", keyword);

        model.addAttribute("activePage", "brands");
        return "admin/brand/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("brand", new Brand());
        model.addAttribute("activePage", "brands");
        return "admin/brand/create";
    }

    @PostMapping("/save")
    public String saveBrand(@ModelAttribute("brand") Brand brand,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            RedirectAttributes redirectAttributes) {
            if (!imageFile.isEmpty()) {

                String logoUrl =
                        uploadService.handleSaveUploadFile(imageFile, "brands");

                brand.setLogoUrl(logoUrl);

            }
            brandService.saveBrand(brand);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hãng thành công!");

        return "redirect:/admin/brands";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Brand brand = brandService.getBrandById(id);
        if (brand == null) {
            return "redirect:/admin/brands";
        }
        model.addAttribute("brand", brand);
        model.addAttribute("activePage", "brands");
        return "admin/brand/edit";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hãng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa hãng này vì đang có sản phẩm liên kết!");
        }
        return "redirect:/admin/brands";
    }
}