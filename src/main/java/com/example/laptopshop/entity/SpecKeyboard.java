package com.example.laptopshop.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "spec_keyboards") @Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecKeyboard {
    @Id @Column(name = "product_id") private Long productId;
    @Column(name = "keyboard_type") private String keyboardType;
    @Column(name = "connection_type") private String connectionType;
    @Column(name = "layout") private String layout;
}