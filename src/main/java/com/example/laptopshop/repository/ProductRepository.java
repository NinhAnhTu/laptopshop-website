package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findBySlug(String slug);
    Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByBrandBrandId(Long brandId, Pageable pageable);
    Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);

    // Hàm filterProducts
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.brandId = :brandId) AND " +
            "(:minPrice IS NULL OR p.salePrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.salePrice <= :maxPrice) AND " +
            "(:minRating IS NULL OR p.rating >= :minRating)")
    List<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Integer minRating
    );

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR p.productName LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.brandId = :brandId) AND " +
            "(:minPrice IS NULL OR p.salePrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.salePrice <= :maxPrice)")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("brandId") Long brandId,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR p.productName LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:status IS NULL OR " +
            " (:status = 'in_stock' AND p.stock > 10) OR " +        // Còn nhiều (>10)
            " (:status = 'low_stock' AND p.stock > 0 AND p.stock <= 10) OR " + // Sắp hết (1-10)
            " (:status = 'out_of_stock' AND p.stock = 0))")        // Hết hàng (0)
    List<Product> searchInventory(@Param("keyword") String keyword,
                                  @Param("status") String status);

    @Query("SELECT od.product FROM OrderDetail od GROUP BY od.product ORDER BY SUM(od.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    List<Product> findTop5ByProductNameContainingIgnoreCase(String productName);

    @Query("SELECT p.productName, SUM(od.quantity) " +
            "FROM OrderDetail od JOIN od.product p " +
            "GROUP BY p.productName " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getTopSellingProductsData(Pageable pageable);
}