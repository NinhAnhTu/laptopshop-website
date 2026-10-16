package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecCaseRepository extends JpaRepository<SpecCase, Long> {
}