package com.example.laptopshop.service;

import com.example.laptopshop.entity.Brand;
import java.util.List;

public interface BrandService {
    List<Brand> getAllBrands();
    Brand getBrandById(Long id);
    Brand saveBrand(Brand brand);
    void deleteBrand(Long id);
}