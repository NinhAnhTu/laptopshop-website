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
    private Long supplierId;
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
}