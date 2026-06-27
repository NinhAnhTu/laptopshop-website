package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Supplier;
import com.example.laptopshop.repository.SupplierRepository;
import com.example.laptopshop.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    public Supplier saveSupplier(Supplier supplier) {
        // 1. Kiểm tra rỗng và format 10 số
        if (supplier.getPhone() == null || supplier.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Lỗi: Số điện thoại không được để trống!");
        }
        if (!supplier.getPhone().matches("^\\d{10}$")) {
            throw new RuntimeException("Lỗi: Số điện thoại phải bao gồm đúng 10 chữ số!");
        }

        // 2. Kiểm tra Unique (Trùng lặp)
        if (supplier.getSupplierId() == null) {
            // Trường hợp THÊM MỚI
            if (supplierRepository.existsByPhone(supplier.getPhone())) {
                throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng cho nhà cung cấp khác!");
            }
        } else {
            // Trường hợp CẬP NHẬT (bỏ qua id hiện tại)
            if (supplierRepository.existsByPhoneAndSupplierIdNot(supplier.getPhone(), supplier.getSupplierId())) {
                throw new RuntimeException("Lỗi: Số điện thoại này đã được sử dụng cho nhà cung cấp khác!");
            }
        }

        return supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    @Override
    public List<Supplier> searchSuppliers(String keyword) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null; // Chuyển chuỗi rỗng thành null để bỏ qua filter
        }
        return supplierRepository.searchSuppliers(keyword);
    }
}