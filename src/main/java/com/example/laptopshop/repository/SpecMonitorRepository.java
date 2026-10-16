package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecMonitorRepository extends JpaRepository<SpecMonitor, Long> {
}
