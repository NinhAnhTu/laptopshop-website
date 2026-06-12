package com.example.laptopshop.job;

import com.example.laptopshop.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VoucherScheduler {

    private final VoucherRepository voucherRepository;

    // Cron job: Chạy vào giây thứ 0 của MỖI PHÚT (VD: 10:00:00, 10:01:00, 10:02:00...)
    @Scheduled(cron = "0 * * * * *")
    public void autoDisableExpiredVouchers() {
        int updatedCount = voucherRepository.disableExpiredVouchers(LocalDateTime.now());

        if (updatedCount > 0) {
            System.out.println("⏳ [Scheduler] Đã tự động khóa " + updatedCount + " voucher hết hạn!");
        }
    }
}