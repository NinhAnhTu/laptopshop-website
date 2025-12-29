package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "warranty_policy")
@Getter
@Setter
public class WarrantyPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    private String policyName;
    private Integer durationMonths; // 12

    @Column(columnDefinition = "TEXT")
    private String description;

    // Một chính sách áp dụng cho nhiều sản phẩm
    @OneToMany(mappedBy = "warrantyPolicy")
    @JsonIgnore
    private List<Product> products;
}
