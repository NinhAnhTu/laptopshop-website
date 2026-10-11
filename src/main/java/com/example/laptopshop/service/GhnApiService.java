package com.example.laptopshop.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GhnApiService {

    // 1. KHAI BÁO THÔNG TIN MÔI TRƯỜNG THẬT (PRODUCTION)
    private static final String GHN_TOKEN = "0ba5db0e-9f1f-426b-9342-c2ed9953098a"; // Token lấy từ sso.ghn.vn
    private static final String SHOP_ID = "6718605"; // ID Cửa hàng thực tế của bạn

    // ĐÃ XÓA CHỮ "dev-" ĐỂ GỌI MÁY CHỦ THẬT
    private static final String GHN_URL_FEE = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private static final String GHN_URL_CREATE = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";

    private final RestTemplate restTemplate = new RestTemplate();

    // ====================================================================
    // HÀM 1: TÍNH PHÍ SHIP (GỌI API THẬT ĐỂ LẤY GIÁ CHÍNH XÁC)
    // ====================================================================
    public int calculateShippingFee(int toDistrictId, String toWardCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("service_type_id", 2); // 2: Giao chuẩn

        // VỊ TRÍ LẤY HÀNG CỦA BẠN (Ví dụ: Thủ Đức, HCM)
        // Nếu thay đổi kho hàng, bạn cần thay đổi 2 ID này
        body.put("from_district_id", 1442);
        body.put("from_ward_code", "20314");

        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("weight", 2500); // Nặng 2.5kg

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GHN_URL_FEE, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

                // Ép kiểu an toàn về số nguyên (VND)
                return Double.valueOf(data.get("total").toString()).intValue();
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("❌ Lỗi API tính phí GHN (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Lỗi Java (Tính phí): " + e.getMessage());
        }

        // Nếu lỗi, trả về 35,000VNĐ
        return 35000;
    }

    // ====================================================================
    // HÀM 2: TẠO ĐƠN HÀNG (MÔ PHỎNG ĐỂ BẢO VỆ ĐỒ ÁN)
    // ====================================================================
    public String createOrderOnGhn(com.example.laptopshop.entity.Order order) {
        /*
         * ĐÃ ĐÓNG GỌI API THẬT ĐỂ TRÁNH SHIPPER ĐẾN NHÀ LẤY HÀNG TEST
         */
        System.out.println("✅ Đã giả lập tạo đơn giao hàng (GHN) cho Order ID: " + order.getOrderId());

        return "GHN-DEMO-" + order.getOrderId(); // Trả về ID giả để hoàn thiện Order
    }
}