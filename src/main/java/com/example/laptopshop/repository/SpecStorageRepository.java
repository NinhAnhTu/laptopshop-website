package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecStorageRepository extends JpaRepository<SpecStorage, Long> {
}