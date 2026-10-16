package com.example.laptopshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spec_mainboards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecMainboard {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "socket_type")
    private String socketType;

    @Column(name = "chipset")
    private String chipset;

    @Column(name = "form_factor")
    private String formFactor;

    @Column(name = "ram_type")
    private String ramType;

    @Column(name = "ram_slots")
    private Integer ramSlots;

    @Column(name = "max_ram_gb")
    private Integer maxRamGb;

    @Column(name = "m2_slots")
    private Integer m2Slots;

    @Column(name = "sata_ports")
    private Integer sataPorts;
}