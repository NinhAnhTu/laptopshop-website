package com.example.laptopshop.repository;

import com.example.laptopshop.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    // Lấy các thành phố thuộc 1 vùng
    List<City> findByRegionRegionId(Long regionId);
}