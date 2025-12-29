package com.example.laptopshop.service;

import com.example.laptopshop.dto.request.ProductCreateDTO;
import com.example.laptopshop.entity.Product;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Page<Product> getAllProducts(int page, int size); // Phân trang
    Product getProductBySlug(String slug);
    Product getProductById(Long id);
    Product createProduct(ProductCreateDTO dto);
    Product updateProduct(Long id, ProductCreateDTO dto);
    void deleteProduct(Long id);
    Page<Product> getAllProducts(String keyword, int page, int size);
    List<Product> filterProducts(Long categoryId, Long brandId, String priceRange, Integer rating);
    List<Product> searchProducts(String keyword, Long categoryId, Long brandId, Double minPrice, Double maxPrice);
    List<Product> searchInventory(String keyword, String status);
    List<Product> getTopSellingProducts(int limit);
    Page<Product> searchProductsByKeyword(String keyword, int page, int size);
}