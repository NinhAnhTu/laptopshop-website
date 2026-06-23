package com.example.laptopshop.observer;

import com.example.laptopshop.event.OrderStatusChangedEvent;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarrantyObserver {

    private final WarrantyService warrantyService;

    @EventListener
    public void handleWarrantyActivation(OrderStatusChangedEvent event) {
        String oldStatus = event.getOldStatus();
        String newStatus = event.getNewStatus();

        boolean isNewStatusDelivered = newStatus != null && newStatus.contains("Đã giao");
        boolean isOldStatusNotDelivered = oldStatus == null || !oldStatus.contains("Đã giao");

        if (isNewStatusDelivered && isOldStatusNotDelivered) {
            try {
                System.out.println("👉 [Observer Bảo Hành] Đang kích hoạt bảo hành cho đơn: " + event.getOrder().getOrderId());
                warrantyService.activateWarranty(event.getOrder());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}