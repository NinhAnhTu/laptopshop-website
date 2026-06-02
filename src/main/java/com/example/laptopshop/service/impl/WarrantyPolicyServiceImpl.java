package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.WarrantyPolicy;
import com.example.laptopshop.repository.WarrantyPolicyRepository;
import com.example.laptopshop.service.WarrantyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarrantyPolicyServiceImpl implements WarrantyPolicyService {

    private final WarrantyPolicyRepository warrantyPolicyRepository;

    @Override
    public List<WarrantyPolicy> getAll() {
        return warrantyPolicyRepository.findAll();
    }

    @Override
    public WarrantyPolicy getById(Long id) {
        return warrantyPolicyRepository.findById(id).orElse(null);
    }

    @Override
    public WarrantyPolicy save(WarrantyPolicy policy) {
        return warrantyPolicyRepository.save(policy);
    }

    @Override
    public void deleteById(Long id) {
        warrantyPolicyRepository.deleteById(id);
    }
}