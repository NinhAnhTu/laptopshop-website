package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_rates")
@Getter
@Setter
public class ShippingRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rateId;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;

    private BigDecimal baseFee;
}
