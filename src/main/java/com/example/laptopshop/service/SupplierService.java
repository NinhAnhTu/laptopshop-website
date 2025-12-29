package com.example.laptopshop.service;

import com.example.laptopshop.entity.Supplier;
import java.util.List;

public interface SupplierService {
    List<Supplier> getAllSuppliers();
    Supplier getSupplierById(Long id);
    Supplier saveSupplier(Supplier supplier);
    void deleteSupplier(Long id);
}