package com.example.laptopshop.service;

import com.example.laptopshop.entity.ProductSerial;
import java.util.List;

public interface ProductSerialService {
    List<ProductSerial> getSerialsByProductId(Long productId);
    void importSerials(Long productId, String serialsInput);
    void deleteSerial(Long serialId);
}