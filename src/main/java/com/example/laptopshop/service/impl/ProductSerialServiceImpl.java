package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.ProductSerial;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.repository.ProductSerialRepository;
import com.example.laptopshop.service.ProductSerialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSerialServiceImpl implements ProductSerialService {

    private final ProductSerialRepository productSerialRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductSerial> getSerialsByProductId(Long productId) {
        return productSerialRepository.findByProduct_ProductId(productId);
    }

    @Override
    @Transactional
    public void importSerials(Long productId, String serialsInput) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        String[] serials = serialsInput.split(",");
        int addedCount = 0;

        for (String sn : serials) {
            String cleanSn = sn.trim();
            if (cleanSn.isEmpty()) continue;

            // Kiểm tra trùng lặp
            if (productSerialRepository.findBySerialNumber(cleanSn).isPresent()) {
                throw new RuntimeException("Serial " + cleanSn + " đã tồn tại trong hệ thống!");
            }

            // Tạo mới Serial
            ProductSerial ps = new ProductSerial();
            ps.setSerialNumber(cleanSn);
            ps.setStatus("AVAILABLE"); // Trạng thái sẵn sàng
            ps.setImportDate(LocalDateTime.now());
            ps.setProduct(product);

            productSerialRepository.save(ps);
            addedCount++;
        }

        product.setStock(product.getStock() + addedCount);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteSerial(Long serialId) {
        ProductSerial ps = productSerialRepository.findById(serialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Serial"));

        if (!"AVAILABLE".equals(ps.getStatus())) {
            throw new RuntimeException("Không thể xóa Serial này vì đã bán hoặc bị lỗi!");
        }

        Product product = ps.getProduct();

        // Xóa serial
        productSerialRepository.delete(ps);

        // Giảm trừ tồn kho
        if (product.getStock() > 0) {
            product.setStock(product.getStock() - 1);
            productRepository.save(product);
        }
    }
}