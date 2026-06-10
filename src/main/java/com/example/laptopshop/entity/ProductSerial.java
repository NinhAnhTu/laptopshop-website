package com.example.laptopshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.laptopshop.entity.enums.SerialStatus;
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

    @Enumerated(EnumType.STRING)
    private SerialStatus status;
    private LocalDateTime importDate; // Ngày nhập kho

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}