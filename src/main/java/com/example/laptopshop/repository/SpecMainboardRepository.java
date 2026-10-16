package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecMainboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecMainboardRepository extends JpaRepository<SpecMainboard, Long> {
}