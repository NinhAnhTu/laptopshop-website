package com.example.laptopshop.job;

import com.example.laptopshop.entity.Warranty;
import com.example.laptopshop.repository.WarrantyRepository;
import com.example.laptopshop.service.EmailService;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WarrantyScheduler {

    private final WarrantyRepository warrantyRepository;
    private final EmailService emailService;
    private final WarrantyService warrantyService;

    // Chạy vào 9:00 sáng mỗi ngày
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanExpiringWarranties() {
        System.out.println("--- BẮT ĐẦU QUÉT BẢO HÀNH SẮP HẾT HẠN ---");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        // Tìm phiếu còn hạn (ACTIVE) và sẽ hết hạn trong 7 ngày tới
        List<Warranty> list = warrantyRepository.findByExpirationDateBetweenAndStatus(now, sevenDaysLater, "ACTIVE");

        if (list.isEmpty()) {
            System.out.println("Không có bảo hành nào sắp hết hạn hôm nay.");
            return;
        }

        for (Warranty w : list) {
            emailService.sendWarrantyExpirationEmail(w);
        }
    }


    @Scheduled(cron = "0 0 1 * * ?") // 1h sáng
    public void updateExpiredStatus() {
    }
    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduledScan() {
        warrantyService.sendWarrantyExpiryReminders();
        warrantyService.scanAndExpireWarranties();
    }
}