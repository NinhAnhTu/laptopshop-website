package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecPsu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecPsuRepository extends JpaRepository<SpecPsu, Long> {
}