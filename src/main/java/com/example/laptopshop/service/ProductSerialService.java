package com.example.laptopshop.service;

import com.example.laptopshop.entity.ProductSerial;
import com.example.laptopshop.entity.enums.SerialStatus;

import java.util.List;

public interface ProductSerialService {
    List<ProductSerial> getSerialsByProductId(Long productId);
    void importSerials(Long productId, String serialsInput);
    void deleteSerial(Long serialId);
    public void updateSerialStatus(Long serialId, SerialStatus newStatus);
}