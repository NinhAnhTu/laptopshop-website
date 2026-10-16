package com.example.laptopshop.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "spec_mice") @Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecMouse {
    @Id @Column(name = "product_id") private Long productId;
    @Column(name = "dpi") private String dpi;
    @Column(name = "connection_type") private String connectionType;
    @Column(name = "sensor") private String sensor;
}