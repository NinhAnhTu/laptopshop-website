package com.example.laptopshop.service;

import com.example.laptopshop.entity.Voucher;
import java.util.List;

public interface VoucherService {
    List<Voucher> getAllVouchers();
    Voucher save(Voucher voucher);
    Voucher getById(Long id);
    void delete(Long id);
    Voucher findByCode(String code);
}