package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecCooler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecCoolerRepository extends JpaRepository<SpecCooler, Long> {
}