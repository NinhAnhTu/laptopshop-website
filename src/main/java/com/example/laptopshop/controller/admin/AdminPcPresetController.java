package com.example.laptopshop.controller.admin;

import com.example.laptopshop.dto.request.PcPresetCreateDTO;
import com.example.laptopshop.entity.PcPreset;
import com.example.laptopshop.service.PcPresetMapper;
import com.example.laptopshop.service.PcPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/pc-presets")
@RequiredArgsConstructor
public class AdminPcPresetController {

    private final PcPresetService presetService;

    // --- Danh sách ---
    @GetMapping
    public String list(Model model) {
        model.addAttribute("presets", presetService.getAllPresets());
        model.addAttribute("activePage", "pc-presets");
        return "admin/pc_preset/list";
    }

    // --- Trang tạo mới (dùng chung template form với trang sửa) ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("preset", null);
        model.addAttribute("activePage", "pc-presets");
        return "admin/pc_preset/form";
    }

    // --- Trang sửa: đổ sẵn linh kiện vào các slot ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        PcPreset preset;
        try {
            preset = presetService.getById(id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/pc-presets";
        }

        List<String> skipped = new ArrayList<>(); // linh kiện không đưa lên slot được (trùng loại / không có slot)
        Map<String, Object> initialBuild = PcPresetMapper.toBuilderBuild(preset, skipped);

        model.addAttribute("preset", preset);
        model.addAttribute("initialBuild", initialBuild);
        model.addAttribute("skippedItems", skipped);
        model.addAttribute("activePage", "pc-presets");
        return "admin/pc_preset/form";
    }

    // --- API lưu (JS gửi JSON). Trả "success" để khớp kiểu xử lý của /cart/add-multiple ---
    @PostMapping
    @ResponseBody
    public ResponseEntity<String> create(@RequestBody PcPresetCreateDTO dto) {
        try {
            presetService.createPreset(dto);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody PcPresetCreateDTO dto) {
        try {
            presetService.updatePreset(id, dto);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // --- Bật / tắt hiển thị ---
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            boolean active = presetService.toggleActive(id);
            ra.addFlashAttribute("successMessage", active ? "Đã hiển thị cấu hình" : "Đã ẩn cấu hình");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/pc-presets";
    }

    // --- Xóa ---
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            presetService.deletePreset(id);
            ra.addFlashAttribute("successMessage", "Đã xóa cấu hình");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/admin/pc-presets";
    }
}