package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecRam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecRamRepository extends JpaRepository<SpecRam, Long> {
}