package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Product;
import com.example.laptopshop.entity.ProductSerial;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.repository.ProductSerialRepository;
import com.example.laptopshop.service.ProductSerialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.laptopshop.entity.enums.SerialStatus;
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
            ps.setStatus(SerialStatus.AVAILABLE); // Trạng thái sẵn sàng
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
    public void updateSerialStatus(Long serialId, SerialStatus newStatus) {
        ProductSerial ps = productSerialRepository.findById(serialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã Serial này"));

        SerialStatus oldStatus = ps.getStatus();

        // CHỐT CHẶN AN TOÀN: Máy đã bán thì không được đổi trạng thái tự phát ở đây
        if (oldStatus == SerialStatus.SOLD || newStatus == SerialStatus.SOLD) {
            throw new RuntimeException("Không thể tự ý thay đổi trạng thái của máy đã bán!");
        }

        if (oldStatus == newStatus) return; // Không có sự thay đổi thì bỏ qua

        Product product = ps.getProduct();

        // LOGIC BIẾN ĐỘNG KHO THỜI GIAN THỰC
        if (oldStatus == SerialStatus.AVAILABLE && newStatus == SerialStatus.DEFECTIVE) {
            // Máy chuyển sang lỗi -> Giảm 1 tồn kho bán hàng
            if (product.getStock() > 0) {
                product.setStock(product.getStock() - 1);
            }
        } else if (oldStatus == SerialStatus.DEFECTIVE && newStatus == SerialStatus.AVAILABLE) {
            // Máy lỗi đã sửa xong -> Cộng lại 1 vào kho bán hàng
            product.setStock(product.getStock() + 1);
        }

        // Cập nhật trạng thái mới cho Serial
        ps.setStatus(newStatus);

        productSerialRepository.save(ps);
        productRepository.save(product); // Đồng bộ lại số lượng kho mới của sản phẩm
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