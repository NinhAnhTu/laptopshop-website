package com.example.laptopshop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class VnPayConfig {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/vnpay-payment-return"; // URL nhận kết quả
    public static String vnp_TmnCode = "THJ6ESMF"; // Mã website
    public static String vnp_HashSecret = "3NYACQDKRK991HXUA43OYMBEB91Z5V3T"; // Chuỗi bí mật
    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    // Hàm tiện ích: Mã hóa dữ liệu (Checksum)
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm tiện ích: Lấy IP người dùng
    public static String getIpAddress(HttpServletRequest request) {
        return "127.0.0.1"; // Hardcode để test trên Localhost
    }

    // Hàm tiện ích: Random chuỗi
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}