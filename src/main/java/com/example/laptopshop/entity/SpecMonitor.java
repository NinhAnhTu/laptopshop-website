package com.example.laptopshop.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "spec_monitors") @Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecMonitor {
    @Id @Column(name = "product_id") private Long productId;
    @Column(name = "screen_size") private String screenSize;
    @Column(name = "resolution") private String resolution;
    @Column(name = "refresh_rate") private String refreshRate;
    @Column(name = "panel_type") private String panelType;
}