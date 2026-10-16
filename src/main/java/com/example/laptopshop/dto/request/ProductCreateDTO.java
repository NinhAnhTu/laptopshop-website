package com.example.laptopshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ProductCreateDTO {
    private String productName;
    @DecimalMin(value = "1", message = "Giá gốc phải lớn hơn 0")
    private BigDecimal originalPrice;

    @DecimalMin(value = "1", message = "Giá bán phải lớn hơn 0")
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

    // --- MAINBOARD (ID: 8) ---
    // (Đã có socketType, ramType ở trên)
    private String chipset;
    // formFactor (Đã có ở Storage, có thể tái sử dụng chung biến formFactor hoặc tạo riêng mainboardFormFactor. Dùng chung cho gọn)
    private Integer ramSlots;
    private Integer maxRamGb;
    private Integer m2Slots;
    private Integer sataPorts;

    // --- PSU (ID: 9) ---
    private Integer wattage;
    private String efficiencyRating;
    private String modularType;
    // formFactor (Dùng chung)

    // --- CASE (ID: 10) ---
    private String maxMainboardSize;
    private Integer maxGpuLengthMm;
    private Integer maxCoolerHeightMm;

    // --- COOLER (ID: 11) ---
    private String coolerType;
    private Integer radiatorSizeMm;
    private Integer heightMm;
    private String supportedSockets;
    private Integer tdpSupportW;

    // --- MONITOR (12) ---
    private String screenSize;
    private String resolution;
    private String refreshRate;
    private String panelType;

    // --- KEYBOARD (13) ---
    private String keyboardType;
    private String connectionType; // Dùng chung cho Phím, Chuột, Loa
    private String layout;

    // --- MOUSE (14) ---
    private String dpi;
    private String sensor;

    // --- SPEAKER (15) ---
    private String powerW;
    //Status
    private Boolean isActive;
}