package com.example.laptopshop.service.impl;

import com.example.laptopshop.dto.request.ProductCreateDTO;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.ProductImage;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.ProductService;
import com.example.laptopshop.util.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarrantyPolicyRepository warrantyPolicyRepository;

    private final UploadService uploadService;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Product createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        // Map dữ liệu cơ bản
        product.setProductName(dto.getProductName());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStock(dto.getStock());
        product.setCpu(dto.getCpu());
        product.setRam(dto.getRam());
        product.setStorage(dto.getStorage());
        product.setGpu(dto.getGpu());
        product.setScreen(dto.getScreen());

        // Map khóa ngoại
        product.setBrand(brandRepository.findById(dto.getBrandId()).orElse(null));
        product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        product.setSupplier(supplierRepository.findById(dto.getSupplierId()).orElse(null));
        product.setWarrantyPolicy(warrantyPolicyRepository.findById(dto.getWarrantyPolicyId()).orElse(null));

        Product savedProduct = productRepository.save(product);

        // 1. Xử lý ảnh Thumbnail
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imageUrl = uploadService.handleSaveUploadFile(dto.getImageFile(), "products");
            ProductImage image = new ProductImage();
            image.setProduct(savedProduct);
            image.setUrl(imageUrl);
            image.setIsThumbnail(true);
            productImageRepository.save(image);
        }

        if (dto.getDetailFiles() != null) {
            for (MultipartFile file : dto.getDetailFiles()) {
                if (!file.isEmpty()) {
                    String detailUrl = uploadService.handleSaveUploadFile(file, "products");
                    ProductImage detailImage = new ProductImage();
                    detailImage.setProduct(savedProduct);
                    detailImage.setUrl(detailUrl);
                    detailImage.setIsThumbnail(false);
                    productImageRepository.save(detailImage);
                }
            }
        }

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductCreateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Cập nhật thông tin cơ bản
        product.setProductName(dto.getProductName());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStock(dto.getStock());
        product.setCpu(dto.getCpu());
        product.setRam(dto.getRam());
        product.setStorage(dto.getStorage());
        product.setGpu(dto.getGpu());
        product.setScreen(dto.getScreen());

        // Cập nhật khóa ngoại
        product.setBrand(brandRepository.findById(dto.getBrandId()).orElse(null));
        product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        product.setSupplier(supplierRepository.findById(dto.getSupplierId()).orElse(null));
        product.setWarrantyPolicy(warrantyPolicyRepository.findById(dto.getWarrantyPolicyId()).orElse(null));

        // Lưu thông tin text trước
        Product savedProduct = productRepository.save(product);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imageUrl = uploadService.handleSaveUploadFile(dto.getImageFile(), "products");

            Optional<ProductImage> oldThumbnail = product.getImages().stream()
                    .filter(img -> img.getIsThumbnail() != null && img.getIsThumbnail())
                    .findFirst();

            if (oldThumbnail.isPresent()) {
                oldThumbnail.get().setUrl(imageUrl);
                productImageRepository.save(oldThumbnail.get());
            } else {
                ProductImage newImage = new ProductImage();
                newImage.setProduct(savedProduct);
                newImage.setUrl(imageUrl);
                newImage.setIsThumbnail(true);
                productImageRepository.save(newImage);
            }
        }

        if (dto.getDetailFiles() != null) {
            for (MultipartFile file : dto.getDetailFiles()) {
                if (!file.isEmpty()) {
                    String detailUrl = uploadService.handleSaveUploadFile(file, "products");
                    ProductImage detailImage = new ProductImage();
                    detailImage.setProduct(savedProduct);
                    detailImage.setUrl(detailUrl);
                    detailImage.setIsThumbnail(false);
                    productImageRepository.save(detailImage);
                }
            }
        }

        return savedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Page<Product> getAllProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        if (keyword != null && !keyword.isEmpty()) {
            // Nếu có từ khóa -> Tìm kiếm
            return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
        } else {
            // Nếu không -> Lấy tất cả
            return productRepository.findAll(pageable);
        }
    }
    @Override
    public List<Product> filterProducts(Long categoryId, Long brandId, String priceRange, Integer rating) {
        Double minPrice = null;
        Double maxPrice = null;

        if (priceRange != null && !priceRange.isEmpty()) {
            if (priceRange.equals("under-10")) {
                maxPrice = 10000000.0;
            } else if (priceRange.equals("10-20")) {
                minPrice = 10000000.0;
                maxPrice = 20000000.0;
            } else if (priceRange.equals("20-30")) {
                minPrice = 20000000.0;
                maxPrice = 30000000.0;
            } else if (priceRange.equals("over-30")) {
                minPrice = 30000000.0;
            }
        }

        return productRepository.filterProducts(categoryId, brandId, minPrice, maxPrice, rating);
    }
    @Override
    public List<Product> searchProducts(String keyword, Long categoryId, Long brandId, Double minPrice, Double maxPrice) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return productRepository.searchProducts(keyword, categoryId, brandId, minPrice, maxPrice);
    }
    // Triển khai hàm
    @Override
    public List<Product> searchInventory(String keyword, String status) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return productRepository.searchInventory(keyword, status);
    }
    @Override
    public List<Product> getTopSellingProducts(int limit) {
        // Lấy trang đầu tiên, số lượng phần tử là limit
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }
    @Override
    public Page<Product> searchProductsByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // Page bắt đầu từ 0
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
    }
}