package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Lấy danh mục theo tên
    Category findByCategoryName(String categoryName);
    // Lấy tất cả danh mục gốc (không có cha)
    List<Category> findByParentIsNull();
}