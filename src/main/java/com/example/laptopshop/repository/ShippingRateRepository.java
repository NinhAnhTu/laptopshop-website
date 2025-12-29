package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, Long> {
    // Tìm giá ship theo vùng
    Optional<ShippingRate> findByRegionRegionId(Long regionId);
}