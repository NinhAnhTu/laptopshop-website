package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pc_preset_items")
@Getter
@Setter
@NoArgsConstructor
public class PcPresetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id", nullable = false)
    private PcPreset preset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    public PcPresetItem(PcPreset preset, Product product, Integer quantity) {
        this.preset = preset;
        this.product = product;
        this.quantity = quantity;
    }

    // ===== Các hàm tiện ích (không map xuống DB) =====

    /** Giá bán hiện tại của 1 sản phẩm (kiểu Number nên dùng được cho Double/BigDecimal/Long). */
    public double unitPrice() {
        if (product == null) return 0;
        Number price = product.getSalePrice();
        return price == null ? 0 : price.doubleValue();
    }

    /** Giá gốc 1 sản phẩm (không bao giờ thấp hơn giá bán, phòng dữ liệu nhập sai). */
    public double unitOriginalPrice() {
        if (product == null) return 0;
        Number original = product.getOriginalPrice();
        double o = original == null ? 0 : original.doubleValue();
        return Math.max(o, unitPrice());
    }

    public double lineOriginalTotal() {
        return unitOriginalPrice() * safeQuantity();
    }

    /** Thành tiền = đơn giá x số lượng. */
    public double lineTotal() {
        return unitPrice() * safeQuantity();
    }

    /** Số bộ tối đa mà linh kiện này còn đáp ứng được (tồn kho / số lượng cần). */
    public int availableSets() {
        if (product == null || !Boolean.TRUE.equals(product.getIsActive())) return 0;
        Number stockNum = product.getStock();
        int stock = stockNum == null ? 0 : stockNum.intValue();
        return stock / safeQuantity();
    }

    /** Ảnh đại diện của linh kiện (ưu tiên ảnh thumbnail), giống logic ở ClientPcBuilderController. */
    public String resolveImageUrl() {
        String imageUrl = "https://via.placeholder.com/150";
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getUrl();
            for (ProductImage img : product.getImages()) {
                if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    imageUrl = img.getUrl();
                    break;
                }
            }
        }
        return imageUrl;
    }

    /** Icon Font Awesome theo loại linh kiện (khớp builderSlots ở pc_builder.html). */
    public String iconClass() {
        if (product == null || product.getCategory() == null || product.getCategory().getCategoryId() == null) {
            return "fa-cube";
        }
        switch (product.getCategory().getCategoryId().intValue()) {
            case 4:  return "fa-memory";
            case 5:  return "fa-hdd";
            case 6:  return "fa-microchip";
            case 7:  return "fa-vr-cardboard";
            case 8:  return "fa-chess-board";
            case 9:  return "fa-bolt";
            case 10: return "fa-box";
            case 11: return "fa-fan";
            case 12: return "fa-desktop";
            case 13: return "fa-keyboard";
            case 14: return "fa-mouse";
            case 15: return "fa-volume-up";
            default: return "fa-cube";
        }
    }

    private int safeQuantity() {
        return (quantity == null || quantity < 1) ? 1 : quantity;
    }
}