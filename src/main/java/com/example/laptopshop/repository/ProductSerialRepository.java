package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    // Tìm serial theo mã (để check trùng khi nhập)
    Optional<ProductSerial> findBySerialNumber(String serialNumber);

    // Lấy danh sách serial của một sản phẩm
    List<ProductSerial> findByProduct_ProductId(Long productId);

    // Lấy danh sách serial CÓ SẴN của một sản phẩm (để đếm tồn kho thực tế)
    List<ProductSerial> findByProduct_ProductIdAndStatus(Long productId, String status);

    // Đếm số lượng tồn kho thực tế của 1 sản phẩm
    long countByProduct_ProductIdAndStatus(Long productId, String status);
}