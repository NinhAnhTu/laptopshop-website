package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spec_vgas")
@Getter
@Setter
public class SpecVga {
    @Id
    @Column(name = "product_id")
    private Long productId;

    private String gpuChip;
    private String vram;
    private String vramType;
    private String powerRecommended;
}