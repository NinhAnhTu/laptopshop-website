package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.Voucher;
import com.example.laptopshop.repository.VoucherRepository;
import com.example.laptopshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher save(Voucher voucher) {
        if (voucher.getCode() != null) {
            voucher.setCode(voucher.getCode().toUpperCase());
        }
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher getById(Long id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public Voucher findByCode(String code) {
        return voucherRepository.findByCode(code).orElse(null);
    }

    @Override
    public List<Voucher> searchVouchers(String keyword, String status) {
        // Chuẩn hóa dữ liệu đầu vào, nếu rỗng thì chuyển thành null để bỏ qua điều kiện lọc
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        return voucherRepository.searchVouchers(keyword, status);
    }
}