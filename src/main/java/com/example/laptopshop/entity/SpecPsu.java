package com.example.laptopshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spec_psus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecPsu {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "wattage")
    private Integer wattage;

    @Column(name = "efficiency_rating")
    private String efficiencyRating;

    @Column(name = "modular_type")
    private String modularType;

    @Column(name = "form_factor")
    private String formFactor;
}