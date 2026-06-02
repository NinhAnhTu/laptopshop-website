package com.example.laptopshop.service;

import com.example.laptopshop.entity.WarrantyPolicy;
import java.util.List;

public interface WarrantyPolicyService {
    List<WarrantyPolicy> getAll();
    WarrantyPolicy getById(Long id);
    WarrantyPolicy save(WarrantyPolicy policy);
    void deleteById(Long id);
}