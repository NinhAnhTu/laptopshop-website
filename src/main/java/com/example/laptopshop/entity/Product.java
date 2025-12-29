package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;
    private String slug;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "warranty_policy_id")
    private WarrantyPolicy warrantyPolicy;

    // Specs
    private String cpu;
    private String ram;
    private String gpu;
    private String storage;
    private Double screen;
    private String upgradeOption;

    // Price & Stock
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private Double rating;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Danh sách ảnh
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;

    // Danh sách serial (kho)
    @OneToMany(mappedBy = "product")
    private List<ProductSerial> serials;

}
