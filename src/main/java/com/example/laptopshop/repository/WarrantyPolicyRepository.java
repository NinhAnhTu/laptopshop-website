package com.example.laptopshop.repository;

import com.example.laptopshop.entity.WarrantyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, Long> {
}