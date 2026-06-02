package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spec_cpus")
@Getter
@Setter
public class SpecCpu {
    @Id
    @Column(name = "product_id")
    private Long productId;

    private String socketType;
    private Integer cores;
    private Integer threads;
    private String baseClock;
    private String boostClock;
    private String tdp;
}