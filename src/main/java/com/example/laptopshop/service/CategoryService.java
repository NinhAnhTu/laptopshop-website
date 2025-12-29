package com.example.laptopshop.service;

import com.example.laptopshop.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
}