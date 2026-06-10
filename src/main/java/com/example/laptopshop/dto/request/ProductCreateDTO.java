package com.example.laptopshop.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductCreateDTO {
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;

    private Long brandId;
    private Long categoryId;
    private Long warrantyPolicyId;

    // Cấu hình
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private Double screen;

    // File ảnh upload
    private MultipartFile imageFile;

    private List<MultipartFile> detailFiles;

    // Thong so cho linh kien roi
    private String ramCapacity;
    private String ramType;
    private String busSpeed;

    private String storageCapacity;
    private String storageType;
    private String formFactor;
    private String readSpeed;
    private String writeSpeed;

    private String socketType;
    private Integer cores;
    private Integer threads;
    private String baseClock;
    private String boostClock;
    private String tdp;

    private String gpuChip;
    private String vram;
    private String vramType;
    private String powerRecommended;

    //Status
    private Boolean isActive;
}