package com.example.laptopshop.repository;

import com.example.laptopshop.entity.WarrantyPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, Long> {
    // Tìm kiếm phân trang
    @Query("SELECT p FROM WarrantyPolicy p WHERE " +
            "(:keyword IS NULL OR LOWER(p.policyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:duration IS NULL OR p.durationMonths = :duration)")
    Page<WarrantyPolicy> searchPolicies(@Param("keyword") String keyword, @Param("duration") Integer duration, Pageable pageable);

    // Kiểm tra trùng tên (Bỏ qua chính sách đang sửa)
    @Query("SELECT COUNT(p) > 0 FROM WarrantyPolicy p WHERE LOWER(p.policyName) = LOWER(:name) AND (:id IS NULL OR p.warrantyId != :id)")
    boolean existsByPolicyNameIgnoreCaseAndWarrantyIdNot(@Param("name") String name, @Param("id") Long id);
}