package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ImportReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
    @Query("SELECT DISTINCT r FROM ImportReceipt r " +
            "LEFT JOIN r.details d " +
            "LEFT JOIN d.product p " +
            "WHERE (:supplierId IS NULL OR r.supplier.supplierId = :supplierId) " +
            "AND (:productName IS NULL OR :productName = '' OR p.productName LIKE CONCAT('%', :productName, '%')) " +
            "AND (:startDate IS NULL OR r.importDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.importDate <= :endDate)")
    Page<ImportReceipt> searchReceipts(@Param("supplierId") Long supplierId,
                                       @Param("productName") String productName,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);
}