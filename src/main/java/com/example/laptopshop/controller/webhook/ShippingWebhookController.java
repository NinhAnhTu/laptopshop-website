package com.example.laptopshop.controller.webhook;

import com.example.laptopshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.laptopshop.dto.request.ShippingUpdateDTO;
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class ShippingWebhookController {

    // Gọi lại OrderService đã có sẵn của hệ thống
    private final OrderService orderService;

    // Secret Token do đối tác cung cấp để bảo mật (Demo có thể gán cứng)
    private static final String GHN_SECRET_TOKEN = "DayLaTokenBaoMat12345";

    @PostMapping("/ghn-update")
    public ResponseEntity<String> handleShippingUpdate(
            @RequestHeader(value = "Token", required = false) String token,
            @RequestBody ShippingUpdateDTO updateData) {

        // 1. Bảo mật: Kiểm tra xem request này có đúng là của GHN không
        if (token == null || !token.equals(GHN_SECRET_TOKEN)) {
            System.out.println("=> [CẢNH BÁO] Có người lạ cố tình gọi API Webhook!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        System.out.println("==> NHẬN WEBHOOK TỪ GIAO HÀNG NHANH <==");
        System.out.println("- Mã đơn hàng hệ thống (ClientOrderCode): " + updateData.getClientOrderCode());
        System.out.println("- Trạng thái từ GHN: " + updateData.getStatus());

        try {
            // Ép kiểu mã đơn hàng bên GHN trả về sang kiểu Long của hệ thống
            Long orderId = Long.parseLong(updateData.getClientOrderCode());

            // 2. Map trạng thái của hệ thống vận chuyển sang trạng thái website của bạn
            String systemStatus = "";
            switch (updateData.getStatus()) {
                case "ready_to_pick":
                    systemStatus = "Đã xác nhận";
                    break;
                case "delivering":
                    systemStatus = "Đang giao";
                    break;
                case "delivered":
                    systemStatus = "Đã giao";
                    break;
                case "cancel":
                case "returned":
                    systemStatus = "Đã hủy";
                    break;
                default:
                    System.out.println("=> Trạng thái chưa được hỗ trợ map: " + updateData.getStatus());
                    return ResponseEntity.ok("Received but ignored");
            }

            // 3. Tự động cập nhật DB thông qua hàm updateOrderStatus đã viết sẵn
            if (!systemStatus.isEmpty()) {
                // Nhờ có EventPublisher trong OrderServiceImpl, cập nhật ở đây sẽ kích hoạt
                // luôn cả vụ hoàn kho (nếu hủy) mà không cần code lại.
                orderService.updateOrderStatus(orderId, systemStatus);
                System.out.println("=> [THÀNH CÔNG] Đã tự động cập nhật đơn hàng #" + orderId + " thành: " + systemStatus);
            }

        } catch (NumberFormatException e) {
            System.out.println("=> [LỖI] Mã đơn hàng không hợp lệ: " + updateData.getClientOrderCode());
        } catch (Exception e) {
            System.out.println("=> [LỖI HỆ THỐNG]: " + e.getMessage());
        }

        // 4. Bắt buộc trả về HTTP 200 OK để bên GHN biết là mình đã nhận được tin báo
        return ResponseEntity.ok("Received");
    }
}

