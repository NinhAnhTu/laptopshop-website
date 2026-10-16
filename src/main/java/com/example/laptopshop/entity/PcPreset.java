package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình PC build sẵn: gồm nhiều linh kiện (Product) có sẵn trong hệ thống.
 * Giá và tồn kho KHÔNG lưu cứng, luôn tính theo linh kiện hiện tại.
 */
@Entity
@Table(name = "pc_presets")
@Getter
@Setter
@NoArgsConstructor
public class PcPreset {

    // categoryId của các linh kiện (khớp với builderSlots trong pc_builder.html)
    private static final long CAT_RAM = 4L;
    private static final long CAT_STORAGE = 5L;
    private static final long CAT_CPU = 6L;
    private static final long CAT_MAINBOARD = 8L;
    private static final long CAT_VGA = 7L;
    private static final long CAT_CASE = 10L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preset_id")
    private Long presetId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "tag", length = 100)
    private String tag;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "preset", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PcPresetItem> items = new ArrayList<>();

    // ===== Các hàm tiện ích (không map xuống DB) =====

    public void addItem(Product product, int quantity) {
        items.add(new PcPresetItem(this, product, quantity));
    }

    /** Tổng tiền theo giá bán hiện tại của từng linh kiện. */
    public double calcTotalPrice() {
        return items.stream().mapToDouble(PcPresetItem::lineTotal).sum();
    }

    /** Tổng giá gốc (để hiện giá gạch ngang). */
    public double calcTotalOriginalPrice() {
        return items.stream().mapToDouble(PcPresetItem::lineOriginalTotal).sum();
    }

    /** Số tiền tiết kiệm được so với giá gốc. */
    public double calcSavings() {
        return Math.max(0, calcTotalOriginalPrice() - calcTotalPrice());
    }

    /** % giảm so với giá gốc (làm tròn). */
    public int calcDiscountPercent() {
        double original = calcTotalOriginalPrice();
        return original <= 0 ? 0 : (int) Math.round(calcSavings() / original * 100);
    }

    /** Số bộ có thể bán = min(tồn kho / số lượng cần) trên tất cả linh kiện. */
    public int calcAvailableQty() {
        return items.stream().mapToInt(PcPresetItem::availableSets).min().orElse(0);
    }

    /** Ảnh hiển thị: ưu tiên ảnh admin nhập, nếu không có thì lấy ảnh của Case, rồi tới linh kiện đầu tiên. */
    public String resolveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) return imageUrl;

        for (PcPresetItem it : items) {
            if (categoryIdOf(it) == CAT_CASE) return it.resolveImageUrl();
        }
        if (!items.isEmpty()) return items.get(0).resolveImageUrl();
        return "https://via.placeholder.com/300";
    }

    /** Các linh kiện chính (CPU, Mainboard, RAM, VGA, ổ cứng) để hiện tóm tắt trên thẻ. */
    public List<PcPresetItem> keyItems() {
        long[] order = {CAT_CPU, CAT_MAINBOARD, CAT_RAM, CAT_VGA, CAT_STORAGE};
        List<PcPresetItem> result = new ArrayList<>();
        for (long cat : order) {
            for (PcPresetItem it : items) {
                if (categoryIdOf(it) == cat) {
                    result.add(it);
                    break;
                }
            }
        }
        return result;
    }

    private long categoryIdOf(PcPresetItem it) {
        if (it.getProduct() == null || it.getProduct().getCategory() == null) return -1L;
        Long id = it.getProduct().getCategory().getCategoryId();
        return id == null ? -1L : id;
    }
}