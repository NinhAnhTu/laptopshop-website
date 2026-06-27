package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Query("SELECT s FROM Supplier s WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.supplierId DESC")
    List<Supplier> searchSuppliers(@Param("keyword") String keyword);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndSupplierIdNot(String phone, Long supplierId);
}