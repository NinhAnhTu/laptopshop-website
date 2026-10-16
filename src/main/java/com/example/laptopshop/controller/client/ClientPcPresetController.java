package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.PcPreset;
import com.example.laptopshop.service.PcPresetMapper;
import com.example.laptopshop.service.PcPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ClientPcPresetController {

    private final PcPresetService presetService;

    // Danh sách cấu hình build sẵn nay nằm ngay trong trang /pc-builder
    @GetMapping("/pc-presets")
    public String list() {
        return "redirect:/pc-builder";
    }

    @GetMapping("/pc-presets/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        PcPreset preset = presetService.getActiveBySlug(slug).orElse(null);
        if (preset == null) return "redirect:/pc-builder";

        model.addAttribute("preset", preset);
        model.addAttribute("cartItems", PcPresetMapper.toCartItems(preset)); // định dạng /cart/add-multiple
        return "client/pc_preset_detail";
    }
}