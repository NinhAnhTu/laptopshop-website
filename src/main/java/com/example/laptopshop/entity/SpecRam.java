package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spec_rams")
@Getter
@Setter
public class SpecRam {
    @Id
    @Column(name = "product_id")
    private Long productId; // Khóa chính đồng thời là ID của Product

    private String capacity;
    private String ramType;
    private String busSpeed;
}