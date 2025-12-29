package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "supplier")
@Getter
@Setter
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    private String supplierName;
    private String contactName;
    private String address;
    private String phone;
    private String email;

    // Một nhà cung cấp cung cấp nhiều sản phẩm
    @OneToMany(mappedBy = "supplier")
    @JsonIgnore
    private List<Product> products;
}
