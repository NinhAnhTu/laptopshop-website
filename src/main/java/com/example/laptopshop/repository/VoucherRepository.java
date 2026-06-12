package com.example.laptopshop.repository;

import com.example.laptopshop.entity.Voucher;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    // Tìm voucher theo mã code
    Optional<Voucher> findByCode(String code);

    // Lệnh Update hàng loạt: Chuyển 'active' thành 'inactive' (hoặc 'expired') nếu thời gian hiện tại đã vượt qua endDate
    @Modifying
    @Transactional
    // Đổi CURRENT_TIMESTAMP thành :now
    @Query("UPDATE Voucher v SET v.status = 'inactive' WHERE v.status = 'active' AND v.endDate <= :now")
    int disableExpiredVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:keyword IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR v.status = :status) " +
            "ORDER BY v.voucherId DESC")
    List<Voucher> searchVouchers(@Param("keyword") String keyword, @Param("status") String status);
}