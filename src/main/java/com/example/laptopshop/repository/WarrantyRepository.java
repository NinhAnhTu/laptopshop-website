package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    @Query("SELECT w FROM Warranty w WHERE " +
            "w.warrantyCode LIKE :keyword OR " +
            "w.user.fullname LIKE :keyword OR " +
            "w.user.phone LIKE :keyword OR " +
            "w.product.productName LIKE :keyword")
    List<Warranty> searchWarranty(@Param("keyword") String keyword);

    List<Warranty> findByExpirationDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, String status);

    Warranty findByWarrantyCode(String code);

    List<Warranty> findByStatus(String status);
}