package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spec_storages")
@Getter
@Setter
public class SpecStorage {
    @Id
    @Column(name = "product_id")
    private Long productId;

    private String capacity;
    private String storageType;
    private String formFactor;
    private String readSpeed;
    private String writeSpeed;
}