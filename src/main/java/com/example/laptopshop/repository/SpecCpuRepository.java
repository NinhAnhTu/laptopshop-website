package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecCpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecCpuRepository extends JpaRepository<SpecCpu, Long> {
}