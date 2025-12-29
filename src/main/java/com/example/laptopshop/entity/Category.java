package com.example.laptopshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String categoryName;


    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore // Tránh vòng lặp vô tận khi lấy dữ liệu JSON
    private Category parent;

    // Một danh mục cha có nhiều danh mục con
    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    // Một danh mục có nhiều sản phẩm
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Product> products;
}
