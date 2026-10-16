package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CompareController {

    private final ProductService productService;

    // Khai báo các Spec Repository để lấy thông số chi tiết
    private final SpecRamRepository specRamRepository;
    private final SpecStorageRepository specStorageRepository;
    private final SpecCpuRepository specCpuRepository;
    private final SpecVgaRepository specVgaRepository;
    private final SpecMainboardRepository specMainboardRepository;
    private final SpecPsuRepository specPsuRepository;
    private final SpecCaseRepository specCaseRepository;
    private final SpecCoolerRepository specCoolerRepository;
    private final SpecMonitorRepository specMonitorRepository;
    private final SpecKeyboardRepository specKeyboardRepository;
    private final SpecMouseRepository specMouseRepository;
    private final SpecSpeakerRepository specSpeakerRepository;

    @GetMapping("/compare")
    public String compareProducts(
            @RequestParam("id1") Long id1,
            @RequestParam("id2") Long id2,
            Model model) {

        // 1. Lấy thông tin cơ bản của 2 sản phẩm
        Product product1 = productService.getProductById(id1);
        Product product2 = productService.getProductById(id2);

        // Xử lý lỗi nếu ID không hợp lệ hoặc sản phẩm bị ẩn/xóa
        if (product1 == null || product2 == null || !product1.getIsActive() || !product2.getIsActive()) {
            model.addAttribute("errorMessage", "Không tìm thấy một trong hai sản phẩm hoặc sản phẩm đã ngừng kinh doanh.");
            return "client/compare"; // Trả về trang so sánh nhưng hiển thị lỗi
        }
        if (!product1.getCompareGroup().equals(product2.getCompareGroup())) {
            model.addAttribute("errorMessage", "Chỉ có thể so sánh các sản phẩm cùng nhóm!");
            return "client/compare"; // giữ đúng tên view hiện tại của bạn
        }

        // 2. Kiểm tra xem 2 sản phẩm có cùng danh mục chính không (VD: Laptop với Laptop, RAM với RAM)
        // Nếu khác danh mục, việc so sánh thông số sẽ rất lộn xộn.
        // Tạm thời có thể cho phép so sánh khác danh mục, nhưng tốt nhất là nên chặn từ Frontend.
        // Ở đây ta cứ truyền dữ liệu, Frontend sẽ xử lý hiển thị.

        // --- NẠP DỮ LIỆU SẢN PHẨM 1 ---
        model.addAttribute("product1", product1);
        Long catId1 = product1.getCategory() != null ? product1.getCategory().getCategoryId() : null;
        loadSpecsToModel(id1, catId1, "1", model);

        // --- NẠP DỮ LIỆU SẢN PHẨM 2 ---
        model.addAttribute("product2", product2);
        Long catId2 = product2.getCategory() != null ? product2.getCategory().getCategoryId() : null;
        loadSpecsToModel(id2, catId2, "2", model);

        return "client/compare";
    }

    /**
     * Hàm phụ trợ để tự động load đúng thông số kỹ thuật (Spec) dựa trên Category ID
     * và gắn vào model với hậu tố phân biệt (ví dụ: "specRam1" hoặc "specRam2")
     */
    private void loadSpecsToModel(Long productId, Long categoryId, String suffix, Model model) {
        if (categoryId == null) return;

        // Tùy theo danh mục mà gọi đúng Repository
        if (categoryId == 4L) {
            specRamRepository.findById(productId).ifPresent(spec -> model.addAttribute("specRam" + suffix, spec));
        } else if (categoryId == 5L) {
            specStorageRepository.findById(productId).ifPresent(spec -> model.addAttribute("specStorage" + suffix, spec));
        } else if (categoryId == 6L) {
            specCpuRepository.findById(productId).ifPresent(spec -> model.addAttribute("specCpu" + suffix, spec));
        } else if (categoryId == 7L) {
            specVgaRepository.findById(productId).ifPresent(spec -> model.addAttribute("specVga" + suffix, spec));
        } else if (categoryId == 8L) {
            specMainboardRepository.findById(productId).ifPresent(spec -> model.addAttribute("specMainboard" + suffix, spec));
        } else if (categoryId == 9L) {
            specPsuRepository.findById(productId).ifPresent(spec -> model.addAttribute("specPsu" + suffix, spec));
        } else if (categoryId == 10L) {
            specCaseRepository.findById(productId).ifPresent(spec -> model.addAttribute("specCase" + suffix, spec));
        } else if (categoryId == 11L) {
            specCoolerRepository.findById(productId).ifPresent(spec -> model.addAttribute("specCooler" + suffix, spec));
        } else if (categoryId == 12L) {
            specMonitorRepository.findById(productId).ifPresent(spec -> model.addAttribute("specMonitor" + suffix, spec));
        } else if (categoryId == 13L) {
            specKeyboardRepository.findById(productId).ifPresent(spec -> model.addAttribute("specKeyboard" + suffix, spec));
        } else if (categoryId == 14L) {
            specMouseRepository.findById(productId).ifPresent(spec -> model.addAttribute("specMouse" + suffix, spec));
        } else if (categoryId == 15L) {
            specSpeakerRepository.findById(productId).ifPresent(spec -> model.addAttribute("specSpeaker" + suffix, spec));
        }
    }
}