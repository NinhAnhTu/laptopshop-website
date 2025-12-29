package com.example.laptopshop.repository;

import com.example.laptopshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Lấy ảnh của một sản phẩm
    List<ProductImage> findByProductProductId(Long productId);
}