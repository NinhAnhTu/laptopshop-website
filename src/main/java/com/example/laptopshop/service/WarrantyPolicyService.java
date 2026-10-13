package com.example.laptopshop.service;

import com.example.laptopshop.entity.WarrantyPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarrantyPolicyService {
    List<WarrantyPolicy> getAll();
    WarrantyPolicy getById(Long id);
    WarrantyPolicy save(WarrantyPolicy policy);
    void deleteById(Long id);

    Page<WarrantyPolicy> searchPolicies(String keyword, Integer duration, Pageable pageable);
    boolean isPolicyNameExists(String name, Long excludeId);
}