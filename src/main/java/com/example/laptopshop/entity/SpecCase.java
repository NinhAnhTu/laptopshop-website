package com.example.laptopshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spec_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecCase {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "max_mainboard_size")
    private String maxMainboardSize;

    @Column(name = "max_gpu_length_mm")
    private Integer maxGpuLengthMm;

    @Column(name = "max_cooler_height_mm")
    private Integer maxCoolerHeightMm;
}