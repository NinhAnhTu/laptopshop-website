package com.example.laptopshop.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GhnApiService {

    // Thay bằng Token và ShopID thật của bạn lấy từ GHN Sandbox
    private static final String GHN_TOKEN = "0ba5db0e-9f1f-426b-9342-c2ed9953098a";
    private static final String SHOP_ID = "6718605";
    private static final String GHN_URL_FEE = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private static final String GHN_URL_CREATE = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. HÀM TÍNH PHÍ SHIP
    public int calculateShippingFee(int toDistrictId, String toWardCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Đóng gói dữ liệu gửi đi (Giả sử laptop nặng 2.5kg)
        Map<String, Object> body = new HashMap<>();
        body.put("service_type_id", 2); // 2: Giao chuẩn
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("weight", 2500); // 2500 gram

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GHN_URL_FEE, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (Integer) data.get("total"); // Trả về số tiền cước (VND)
            }
        } catch (Exception e) {
            System.out.println("Lỗi tính phí GHN: " + e.getMessage());
        }
        return 35000; // Trả về phí mặc định nếu API lỗi
    }

    // 2. HÀM TẠO ĐƠN HÀNG TRÊN GHN
    public String createOrderOnGhn(com.example.laptopshop.entity.Order order) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("payment_type_id", order.getPaymentMethod().getMethodId() == 1 ? 2 : 1); // 2: Khách trả ship, 1: Shop trả
        body.put("note", order.getNote());
        body.put("required_note", "CHOXEMHANGKHONGTHU");
        body.put("client_order_code", String.valueOf(order.getOrderId())); // [QUAN TRỌNG] ID Đơn hàng của bạn để móc nối Webhook
        body.put("to_name", order.getShippingName());
        body.put("to_phone", order.getShippingPhone());
        body.put("to_address", order.getShippingAddress());

        // Fix cứng quận/phường test, thực tế phải lấy từ DB của bạn
        body.put("to_ward_code", "20308");
        body.put("to_district_id", 1442);

        body.put("weight", 2500);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GHN_URL_CREATE, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("order_code"); // Trả về mã vận đơn của GHN (Vd: GHN-12345)
            }
        } catch (Exception e) {
            System.out.println("Lỗi tạo đơn GHN: " + e.getMessage());
        }
        return null;
    }
}