package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecVga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecVgaRepository extends JpaRepository<SpecVga, Long> {
}