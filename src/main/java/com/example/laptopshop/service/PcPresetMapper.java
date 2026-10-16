package com.example.laptopshop.service;

import com.example.laptopshop.entity.PcPreset;
import com.example.laptopshop.entity.PcPresetItem;
import com.example.laptopshop.entity.Product;

import java.util.*;

import static java.util.Map.entry;

/**
 * Chuyển PcPreset sang các cấu trúc mà JS của trang pc_builder.html đang dùng:
 *  - build : { slotId: { product: {...}, quantity } }   (giống currentBuild trong builder)
 *  - cart  : [ { productId, quantity } ]                (giống body của /cart/add-multiple)
 */
public final class PcPresetMapper {

    private PcPresetMapper() {}

    // categoryId -> id slot trong builder (khớp builderSlots ở pc_builder.html)
    public static final Map<Long, String> SLOT_BY_CATEGORY = Map.ofEntries(
            entry(6L, "cpu"), entry(8L, "mainboard"), entry(4L, "ram"), entry(7L, "vga"),
            entry(5L, "storage"), entry(9L, "psu"), entry(10L, "case"), entry(11L, "cooler"),
            entry(12L, "monitor"), entry(13L, "keyboard"), entry(14L, "mouse"), entry(15L, "speaker"));

    /**
     * @param skippedNames nếu khác null, sẽ được thêm tên các linh kiện KHÔNG đưa lên slot được
     *                     (trùng loại với linh kiện khác hoặc không thuộc slot nào)
     */
    public static Map<String, Object> toBuilderBuild(PcPreset preset, List<String> skippedNames) {
        Map<String, Object> build = new LinkedHashMap<>();
        for (PcPresetItem it : preset.getItems()) {
            Product p = it.getProduct();
            Long catId = (p.getCategory() == null) ? null : p.getCategory().getCategoryId();
            String slot = (catId == null) ? null : SLOT_BY_CATEGORY.get(catId);

            if (slot == null || build.containsKey(slot)) {
                if (skippedNames != null) skippedNames.add(p.getProductName());
                continue;
            }

            Map<String, Object> product = new HashMap<>();
            product.put("productId", p.getProductId());
            product.put("productName", p.getProductName());
            product.put("slug", p.getSlug());
            product.put("salePrice", p.getSalePrice());
            product.put("originalPrice", p.getOriginalPrice());
            product.put("stock", p.getStock());
            product.put("imageUrl", it.resolveImageUrl());

            Map<String, Object> slotValue = new HashMap<>();
            slotValue.put("product", product);
            slotValue.put("quantity", it.getQuantity());
            build.put(slot, slotValue);
        }
        return build;
    }

    public static List<Map<String, Object>> toCartItems(PcPreset preset) {
        List<Map<String, Object>> cart = new ArrayList<>();
        for (PcPresetItem it : preset.getItems()) {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", it.getProduct().getProductId());
            m.put("quantity", it.getQuantity());
            cart.add(m);
        }
        return cart;
    }
}