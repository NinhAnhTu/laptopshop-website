package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_serials")
@Getter
@Setter
public class ProductSerial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serialId;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    private String status;

    private LocalDateTime importDate; // Ngày nhập kho

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}