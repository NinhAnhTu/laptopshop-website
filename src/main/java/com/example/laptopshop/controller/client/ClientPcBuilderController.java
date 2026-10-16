package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Brand;
import com.example.laptopshop.entity.PcPreset;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.ProductImage;
import com.example.laptopshop.repository.BrandRepository;
import com.example.laptopshop.service.PcPresetMapper;
import com.example.laptopshop.service.PcPresetService;
import com.example.laptopshop.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ClientPcBuilderController {

    private final ProductService productService;
    private final BrandRepository brandRepository;
    private final PcPresetService presetService;

    /** 3 tab cấu hình build sẵn. "key" phải khớp giá trị tag admin chọn trong form. */
    @Getter
    @AllArgsConstructor
    public static class PresetTab {
        private final String key;
        private final String title;
        private final String subtitle;
    }

    private static final List<PresetTab> PRESET_TABS = List.of(
            new PresetTab("Gaming", "PC GAMING", "Khám phá ngay!"),
            new PresetTab("Creator", "PC ĐỒ HỌA", "Ưu đãi ngập tràn"),
            new PresetTab("Office", "PC VĂN PHÒNG", "Khám phá ngay!"));

    @GetMapping("/pc-builder")
    public String showPcBuilderPage(Model model) {
        Map<String, List<PcPreset>> presetsByTag = new LinkedHashMap<>();
        for (PresetTab tab : PRESET_TABS) presetsByTag.put(tab.getKey(), new ArrayList<>());

        // Dữ liệu cho JS: presetId -> { name, build (để nạp vào builder), cart (để thêm giỏ) }
        Map<Long, Map<String, Object>> presetData = new LinkedHashMap<>();

        for (PcPreset preset : presetService.getActivePresets()) {
            List<PcPreset> bucket = presetsByTag.get(preset.getTag());
            if (bucket == null) continue; // tag không thuộc 3 tab thì không hiển thị

            bucket.add(preset);

            Map<String, Object> data = new HashMap<>();
            data.put("name", preset.getName());
            data.put("build", PcPresetMapper.toBuilderBuild(preset, null));
            data.put("cart", PcPresetMapper.toCartItems(preset));
            presetData.put(preset.getPresetId(), data);
        }

        model.addAttribute("presetTabs", PRESET_TABS);
        model.addAttribute("presetsByTag", presetsByTag);
        model.addAttribute("presetData", presetData);
        return "client/pc_builder";
    }

    @GetMapping("/api/builder/products")
    @ResponseBody
    public Map<String, Object> getProductsForBuilder(@RequestParam Long categoryId) {
        List<Product> products = productService.filterProducts(categoryId, null, null, null);
        List<Brand> brands = brandRepository.findAll();

        List<Map<String, Object>> responseProducts = new ArrayList<>();
        List<Map<String, Object>> responseBrands = new ArrayList<>();

        if (products != null) {
            for (Product p : products) {
                if (Boolean.TRUE.equals(p.getIsActive()) && p.getStock() != null && p.getStock() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", p.getProductId());
                    map.put("productName", p.getProductName());
                    map.put("slug", p.getSlug());
                    map.put("salePrice", p.getSalePrice());
                    map.put("originalPrice", p.getOriginalPrice());
                    map.put("stock", p.getStock());
                    map.put("brandId", p.getBrand() != null ? p.getBrand().getBrandId() : null);

                    String imageUrl = "https://via.placeholder.com/150";
                    if (p.getImages() != null && !p.getImages().isEmpty()) {
                        imageUrl = p.getImages().get(0).getUrl();
                        for (ProductImage img : p.getImages()) {
                            if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                                imageUrl = img.getUrl();
                                break;
                            }
                        }
                    }
                    map.put("imageUrl", imageUrl);
                    responseProducts.add(map);
                }
            }
        }

        if (brands != null) {
            for (Brand b : brands) {
                Map<String, Object> map = new HashMap<>();
                map.put("brandId", b.getBrandId());
                map.put("brandName", b.getBrandName());
                responseBrands.add(map);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", responseProducts);
        response.put("brands", responseBrands);
        return response;
    }
}