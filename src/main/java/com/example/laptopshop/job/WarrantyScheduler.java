package com.example.laptopshop.job;

import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarrantyScheduler {

    private final WarrantyService warrantyService;

    /**
     * 1. Tự động khóa bảo hành đã quá hạn
     * Cron: "0 0 0 * * *" nghĩa là chạy vào đúng 00:00:00 (Nửa đêm) mỗi ngày.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void autoExpireWarranties() {
        System.out.println("⏳ [Scheduler - 00:00] Đang quét cập nhật trạng thái bảo hành hết hạn...");
        warrantyService.scanAndExpireWarranties();
        System.out.println("✅ [Scheduler] Đã cập nhật xong trạng thái bảo hành!");
    }

    /**
     * 2. Tự động gửi Email nhắc nhở sắp hết hạn (trước 7 ngày)
     * Cron: "0 0 8 * * *" nghĩa là chạy vào đúng 08:00:00 (8h Sáng) mỗi ngày.
     * Gửi buổi sáng để khách hàng dễ đọc email hơn là gửi lúc nửa đêm.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void autoSendWarrantyReminders() {
        System.out.println("⏳ [Scheduler - 08:00] Đang quét bảo hành sắp hết hạn để gửi Email...");
        warrantyService.sendWarrantyExpiryReminders();
        System.out.println("✅ [Scheduler] Hoàn tất gửi Email nhắc nhở bảo hành!");
    }
}