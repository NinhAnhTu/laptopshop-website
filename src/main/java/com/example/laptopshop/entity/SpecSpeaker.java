package com.example.laptopshop.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "spec_speakers") @Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecSpeaker {
    @Id @Column(name = "product_id") private Long productId;
    @Column(name = "power_w") private String powerW;
    @Column(name = "connection_type") private String connectionType;
}