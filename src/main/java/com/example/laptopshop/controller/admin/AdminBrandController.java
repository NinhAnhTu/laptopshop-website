package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Brand;
import com.example.laptopshop.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    // Đường dẫn lưu ảnh ra thư mục gốc "uploads" để hiện ngay lập tức
    private final String UPLOAD_DIR = "uploads/brands/";

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
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
        try {
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);

                // Tạo thư mục nếu chưa có
                if (!Files.exists(Paths.get(UPLOAD_DIR))) {
                    Files.createDirectories(Paths.get(UPLOAD_DIR));
                }

                // Lưu file vật lý
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                brand.setLogoUrl("/uploads/brands/" + fileName);
            }
            brandService.saveBrand(brand);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hãng thành công!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi upload ảnh: " + e.getMessage());
        }

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