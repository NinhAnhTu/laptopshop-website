package com.example.laptopshop.service.impl;

import com.example.laptopshop.dto.request.ProductCreateDTO;
import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.ProductService;
import com.example.laptopshop.util.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WarrantyPolicyRepository warrantyPolicyRepository;

    private final SpecRamRepository specRamRepository;
    private final SpecStorageRepository specStorageRepository;
    private final SpecCpuRepository specCpuRepository;
    private final SpecVgaRepository specVgaRepository;

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
        if (dto.getSalePrice() != null && dto.getOriginalPrice() != null
                && dto.getSalePrice().compareTo(dto.getOriginalPrice()) > 0) {
            throw new RuntimeException("Lỗi: Giá bán khuyến mãi không được cao hơn giá gốc!");
        }
        if (dto.getStock() != null && dto.getStock() < 0) {
            throw new RuntimeException("Lỗi: Số lượng tồn kho không được là số âm!");
        }
        Product product = new Product();
        // Map dữ liệu cơ bản
        product.setProductName(dto.getProductName());
        String slug = dto.getProductName().toLowerCase().replace(" ", "-");
        product.setSlug(slug);
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStock(0);
        product.setCpu(dto.getCpu());
        product.setRam(dto.getRam());
        product.setStorage(dto.getStorage());
        product.setGpu(dto.getGpu());
        product.setScreen(dto.getScreen());
        product.setIsActive(false);

        // Map khóa ngoại
        product.setBrand(brandRepository.findById(dto.getBrandId()).orElse(null));
        product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        product.setWarrantyPolicy(warrantyPolicyRepository.findById(dto.getWarrantyPolicyId()).orElse(null));

        Product savedProduct = productRepository.save(product);
// --- ĐOẠN CODE THÊM MỚI: LƯU THÔNG SỐ LINH KIỆN TƯƠNG ỨNG ---
        Long catId = dto.getCategoryId();
        if (catId != null) {
            if (catId == 6L) { // RAM
                SpecRam ram = new SpecRam();
                ram.setProductId(savedProduct.getProductId());
                ram.setCapacity(dto.getRamCapacity());
                ram.setRamType(dto.getRamType());
                ram.setBusSpeed(dto.getBusSpeed());
                specRamRepository.save(ram);
            } else if (catId == 7L) { // SSD/HDD
                SpecStorage storage = new SpecStorage();
                storage.setProductId(savedProduct.getProductId());
                storage.setCapacity(dto.getStorageCapacity());
                storage.setStorageType(dto.getStorageType());
                storage.setFormFactor(dto.getFormFactor());
                storage.setReadSpeed(dto.getReadSpeed());
                storage.setWriteSpeed(dto.getWriteSpeed());
                specStorageRepository.save(storage);
            } else if (catId == 8L) { // CPU
                SpecCpu cpu = new SpecCpu();
                cpu.setProductId(savedProduct.getProductId());
                cpu.setSocketType(dto.getSocketType());
                cpu.setCores(dto.getCores());
                cpu.setThreads(dto.getThreads());
                cpu.setBaseClock(dto.getBaseClock());
                cpu.setBoostClock(dto.getBoostClock());
                cpu.setTdp(dto.getTdp());
                specCpuRepository.save(cpu);
            } else if (catId == 9L) { // VGA
                SpecVga vga = new SpecVga();
                vga.setProductId(savedProduct.getProductId());
                vga.setGpuChip(dto.getGpuChip());
                vga.setPowerRecommended(dto.getPowerRecommended());
                vga.setVram(dto.getVram());
                vga.setVramType(dto.getVramType());
                specVgaRepository.save(vga);
            }
        }
        // -------------------------------------------------------------
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
        product.setCpu(dto.getCpu());
        product.setRam(dto.getRam());
        product.setStorage(dto.getStorage());
        product.setGpu(dto.getGpu());
        product.setScreen(dto.getScreen());
        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }
        // Cập nhật khóa ngoại
        product.setBrand(brandRepository.findById(dto.getBrandId()).orElse(null));
        product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        product.setWarrantyPolicy(warrantyPolicyRepository.findById(dto.getWarrantyPolicyId()).orElse(null));

        // Lưu thông tin text trước
        Product savedProduct = productRepository.save(product);
// --- ĐOẠN CODE THÊM MỚI: CẬP NHẬT THÔNG SỐ LINH KIỆN ---
        Long catId = dto.getCategoryId();
        if (catId != null) {
            if (catId == 6L) {
                // Tìm record cũ, nếu ko có thì tạo mới
                SpecRam ram = specRamRepository.findById(id).orElse(new SpecRam());
                ram.setProductId(id);
                ram.setCapacity(dto.getRamCapacity());
                ram.setRamType(dto.getRamType());
                ram.setBusSpeed(dto.getBusSpeed());
                specRamRepository.save(ram);
            } else if (catId == 7L) {
                SpecStorage storage = specStorageRepository.findById(id).orElse(new SpecStorage());
                storage.setProductId(id);
                storage.setCapacity(dto.getStorageCapacity());
                storage.setStorageType(dto.getStorageType());
                storage.setFormFactor(dto.getFormFactor());
                storage.setReadSpeed(dto.getReadSpeed());
                storage.setWriteSpeed(dto.getWriteSpeed());
                specStorageRepository.save(storage);
            } else if (catId == 8L) {
                SpecCpu cpu = specCpuRepository.findById(id).orElse(new SpecCpu());
                cpu.setProductId(id);
                cpu.setSocketType(dto.getSocketType());
                cpu.setCores(dto.getCores());
                cpu.setThreads(dto.getThreads());
                cpu.setBaseClock(dto.getBaseClock());
                cpu.setBoostClock(dto.getBoostClock());
                cpu.setTdp(dto.getTdp());
                specCpuRepository.save(cpu);
            } else if (catId == 9L) {
                SpecVga vga = specVgaRepository.findById(id).orElse(new SpecVga());
                vga.setProductId(id);
                vga.setGpuChip(dto.getGpuChip());
                vga.setPowerRecommended(dto.getPowerRecommended());
                vga.setVram(dto.getVram());
                vga.setVramType(dto.getVramType());
                specVgaRepository.save(vga);
            }
        }
        // --------------------------------------------------------
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

        // --- BẮN SỰ KIỆN REALTIME CHO KHÁCH HÀNG ---
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("productId", savedProduct.getProductId());

        if (Boolean.TRUE.equals(savedProduct.getIsActive())) {
            // NẾU BẬT ACTIVE -> Gửi thông tin để vẽ Card sản phẩm
            payload.put("action", "SHOW");
            payload.put("productName", savedProduct.getProductName());
            payload.put("slug", savedProduct.getSlug());
            payload.put("salePrice", savedProduct.getSalePrice());

            // Lấy URL ảnh Thumbnail (hoặc ảnh placeholder nếu chưa có)
            String thumbUrl = "https://via.placeholder.com/300";
            if (savedProduct.getImages() != null && !savedProduct.getImages().isEmpty()) {
                thumbUrl = savedProduct.getImages().stream()
                        .filter(img -> img.getIsThumbnail() != null && img.getIsThumbnail())
                        .findFirst()
                        .map(ProductImage::getUrl)
                        .orElse(savedProduct.getImages().get(0).getUrl());
            }
            payload.put("imageUrl", thumbUrl);
        } else {
            // NẾU TẮT ACTIVE -> Gửi lệnh HIDE để Khách hàng ẩn đi
            payload.put("action", "HIDE");
        }

        // Bắn vào kênh /topic/product-updates
        messagingTemplate.convertAndSend("/topic/product-updates", payload);
        // -------------------------------------------

        return savedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Page<Product> getActiveProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByProductNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
        }
        return productRepository.findByIsActiveTrue(pageable);
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

    @Override
    public List<Product> getTopSellingActiveProducts(int limit) {
        return productRepository.findTopSellingActiveProducts(PageRequest.of(0, limit));
    }
    @Override
    public Page<Product> searchProductsByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // Page bắt đầu từ 0
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
    }
}