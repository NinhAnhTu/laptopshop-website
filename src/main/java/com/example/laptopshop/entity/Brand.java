package com.example.laptopshop.entity;

import jakarta.persistence.*; // Quan trọng: Phải là jakarta, KHÔNG dùng javax
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "brand")
@Getter
@Setter
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;

    private String brandName;
    private String country;
    private String logoUrl;

    @OneToMany(mappedBy = "brand")
    @JsonIgnore
    private List<Product> products;
}