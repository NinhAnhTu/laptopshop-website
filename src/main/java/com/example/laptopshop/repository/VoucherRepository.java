package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    // Tìm voucher theo mã code
    Optional<Voucher> findByCode(String code);
}