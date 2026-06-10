package com.example.laptopshop.service.impl;

import com.example.laptopshop.dto.request.ImportReceiptDTO;
import com.example.laptopshop.dto.request.ImportReceiptDetailDTO;
import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.ImportReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.laptopshop.entity.enums.SerialStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportReceiptServiceImpl implements ImportReceiptService {

    private final ImportReceiptRepository importReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductSerialRepository productSerialRepository;

    @Override
    public List<ImportReceipt> getAllReceipts() {
        return importReceiptRepository.findAll();
    }

    @Override
    public ImportReceipt getReceiptById(Long id) {
        return importReceiptRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ImportReceipt saveImportReceipt(ImportReceiptDTO dto, User creator) {
        // 1. Khởi tạo đối tượng Phiếu nhập
        ImportReceipt receipt = new ImportReceipt();
        receipt.setNote(dto.getNote());
        receipt.setCreatedBy(creator);

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp phù hợp"));
        receipt.setSupplier(supplier);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ImportReceiptDetail> details = new ArrayList<>();

        // 2. Duyệt qua từng dòng sản phẩm được nhập trong Form
        for (ImportReceiptDetailDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + itemDto.getProductId()));

            // Cắt nhỏ chuỗi Serial nhận từ textarea bằng dấu phẩy hoặc xuống dòng
            String[] serials = itemDto.getSerialList().split("[,\\r\\n]+");
            List<String> validSerials = new ArrayList<>();
            for (String s : serials) {
                if (s != null && !s.trim().isEmpty()) {
                    validSerials.add(s.trim());
                }
            }

            // CHỐT CHẶN: Kiểm tra số lượng dòng Serial có khớp chính xác với số lượng khai báo không
            if (validSerials.size() != itemDto.getQuantity()) {
                throw new RuntimeException("Lỗi nhập kho sản phẩm [" + product.getProductName() + "]: Số lượng Serial nhập vào ("
                        + validSerials.size() + ") không khớp với Số lượng sản phẩm khai báo (" + itemDto.getQuantity() + ")!");
            }

            // Tạo đối tượng Chi tiết phiếu nhập
            ImportReceiptDetail detail = new ImportReceiptDetail();
            detail.setImportReceipt(receipt);
            detail.setProduct(product);
            detail.setQuantity(itemDto.getQuantity());
            detail.setImportPrice(itemDto.getImportPrice());
            details.add(detail);

            // Tính toán cộng dồn tổng tiền của cả phiếu nhập
            BigDecimal itemTotal = itemDto.getImportPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // TỰ ĐỘNG CẬP NHẬT KHO: Cộng dồn số lượng vào bảng products
            product.setStock(product.getStock() + itemDto.getQuantity());
            productRepository.save(product);

            // TỰ ĐỘNG TẠO SERIAL: Duyệt mảng để lưu từng cái máy cụ thể vào bảng product_serials
            for (String serialNumber : validSerials) {
                // Kiểm tra xem mã Serial này đã từng tồn tại trong hệ thống chưa để tránh trùng lặp trùng khóa UNIQUE
                if (productSerialRepository.findBySerialNumber(serialNumber).isPresent()) {
                    throw new RuntimeException("Mã Serial [" + serialNumber + "] đã tồn tại trong hệ thống! Không thể nhập trùng.");
                }

                ProductSerial productSerial = new ProductSerial();
                productSerial.setProduct(product);
                productSerial.setSerialNumber(serialNumber);
                productSerial.setStatus(SerialStatus.AVAILABLE);
                productSerialRepository.save(productSerial);
            }
        }

        receipt.setTotalAmount(totalAmount);
        receipt.setDetails(details);

        // Lưu phiếu nhập và tự động cascade lưu chi tiết phiếu nhập
        return importReceiptRepository.save(receipt);
    }

    @Override
    public Page<ImportReceipt> searchReceipts(Long supplierId, String productName, LocalDate startDate, LocalDate endDate, int page, int size) {
        // Phân trang, sắp xếp phiếu nhập mới nhất lên đầu
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("importDate").descending());

        // Chuyển đổi LocalDate (chỉ có ngày) sang LocalDateTime (ngày + giờ) để quét trọn vẹn trong ngày
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        return importReceiptRepository.searchReceipts(supplierId, productName, start, end, pageable);
    }
}