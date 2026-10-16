package com.example.laptopshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spec_coolers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecCooler {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "cooler_type")
    private String coolerType;

    @Column(name = "radiator_size_mm")
    private Integer radiatorSizeMm;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Column(name = "supported_sockets")
    private String supportedSockets;

    @Column(name = "tdp_support_w")
    private Integer tdpSupportW;
}