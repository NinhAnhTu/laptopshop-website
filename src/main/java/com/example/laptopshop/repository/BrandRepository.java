package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Brand findByBrandName(String brandName);

    // THÊM HÀM SEARCH NÀY: Tìm theo tên hãng HOẶC quốc gia
    @Query("SELECT b FROM Brand b WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(b.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.country) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY b.brandId DESC")
    List<Brand> searchBrands(@Param("keyword") String keyword);
}