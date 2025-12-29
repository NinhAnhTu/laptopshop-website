package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Cart;
import com.example.laptopshop.entity.CartDetail;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.Voucher;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.UserService;
import com.example.laptopshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/apply")
    public ResponseEntity<?> applyVoucher(@RequestParam("code") String code,
                                          @RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
                                          Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        // 1. Lấy User chuẩn
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            response.put("valid", false);
            response.put("message", "Vui lòng đăng nhập!");
            return ResponseEntity.ok(response);
        }

        // 2. Tìm Voucher
        Voucher voucher = voucherService.findByCode(code);
        if (voucher == null) {
            response.put("valid", false);
            response.put("message", "Mã không tồn tại!");
            return ResponseEntity.ok(response);
        }

        // 3. Kiểm tra thời hạn và trạng thái
        LocalDateTime now = LocalDateTime.now();
        if (!"active".equals(voucher.getStatus()) || now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            response.put("valid", false);
            response.put("message", "Mã không khả dụng vào lúc này!");
            return ResponseEntity.ok(response);
        }
        if (voucher.getQuantity() <= 0) {
            response.put("valid", false);
            response.put("message", "Mã đã hết lượt sử dụng!");
            return ResponseEntity.ok(response);
        }

        // Nếu khách chưa đủ doanh số thì không cho dùng các mã VIP này
        BigDecimal totalSpent = orderService.calculateTotalSpent(user);
        long vId = voucher.getVoucherId();

        if (vId == 1 && totalSpent.compareTo(new BigDecimal("20000000")) < 0) {
            response.put("valid", false);
            response.put("message", "Bạn cần tích lũy mua sắm trên 20 triệu để dùng mã này!");
            return ResponseEntity.ok(response);
        }
        if (vId == 2 && totalSpent.compareTo(new BigDecimal("30000000")) < 0) {
            response.put("valid", false);
            response.put("message", "Bạn cần tích lũy mua sắm trên 30 triệu để dùng mã này!");
            return ResponseEntity.ok(response);
        }
        if (vId == 3 && totalSpent.compareTo(new BigDecimal("40000000")) < 0) {
            response.put("valid", false);
            response.put("message", "Bạn cần tích lũy mua sắm trên 40 triệu để dùng mã này!");
            return ResponseEntity.ok(response);
        }

        // 5. Tính toán tổng tiền hàng hiện tại
        Cart cart = cartService.getCartByUser(user);
        BigDecimal currentOrderTotal = BigDecimal.ZERO;
        if (cart != null && cart.getCartDetails() != null) {
            for (CartDetail item : cart.getCartDetails()) {
                if (selectedIds == null || selectedIds.isEmpty() || selectedIds.contains(item.getCartDetailId())) {
                    currentOrderTotal = currentOrderTotal.add(item.getProduct().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }

        // 6. Kiểm tra đơn tối thiểu của voucher (đối với đơn hàng hiện tại)
        if (currentOrderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            response.put("valid", false);
            response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu: " + String.format("%,.0f", voucher.getMinOrderValue()) + "đ");
            return ResponseEntity.ok(response);
        }

        // 7. Tính tiền giảm giá
        BigDecimal discountAmount = currentOrderTotal.multiply(voucher.getDiscountPercent().divide(BigDecimal.valueOf(100)));
        if (discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discountAmount = voucher.getMaxDiscountAmount();
        }

        response.put("valid", true);
        response.put("message", "Áp dụng thành công!");
        response.put("discountAmount", discountAmount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableVouchers(@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
                                                  Authentication authentication) {

        User user = getUserFromAuthentication(authentication);
        if (user == null) return ResponseEntity.badRequest().body("Chưa đăng nhập");

        BigDecimal totalSpent = orderService.calculateTotalSpent(user);

        boolean level1 = totalSpent.compareTo(new BigDecimal("20000000")) >= 0; // > 20tr
        boolean level2 = totalSpent.compareTo(new BigDecimal("30000000")) >= 0; // > 30tr
        boolean level3 = totalSpent.compareTo(new BigDecimal("40000000")) >= 0; // > 40tr

        Cart cart = cartService.getCartByUser(user);
        BigDecimal currentOrderTotal = BigDecimal.ZERO;
        if (cart != null && cart.getCartDetails() != null) {
            for (CartDetail item : cart.getCartDetails()) {
                if (selectedIds == null || selectedIds.isEmpty() || selectedIds.contains(item.getCartDetailId())) {
                    currentOrderTotal = currentOrderTotal.add(item.getProduct().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        final BigDecimal finalTotal = currentOrderTotal;

        // 3. Lọc và trả về danh sách
        List<Voucher> allVouchers = voucherService.getAllVouchers();
        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> result = allVouchers.stream()
                .filter(v -> "active".equals(v.getStatus()))
                .filter(v -> v.getQuantity() > 0)
                .filter(v -> now.isAfter(v.getStartDate()) && now.isBefore(v.getEndDate()))

                .filter(v -> {
                    long vid = v.getVoucherId();
                    if (vid == 1) return level1;
                    if (vid == 2) return level2;
                    if (vid == 3) return level3;
                    return true;
                })

                .map(v -> {
                    BigDecimal percent = v.getDiscountPercent().divide(BigDecimal.valueOf(100));
                    BigDecimal discount = finalTotal.multiply(percent);
                    // Giới hạn giảm tối đa
                    if (discount.compareTo(v.getMaxDiscountAmount()) > 0) {
                        discount = v.getMaxDiscountAmount();
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("code", v.getCode());
                    map.put("discountPercent", v.getDiscountPercent());
                    map.put("maxDiscount", v.getMaxDiscountAmount());
                    map.put("minOrder", v.getMinOrderValue());
                    map.put("endDate", v.getEndDate());
                    map.put("quantity", v.getQuantity());
                    map.put("discountAmount", discount);
                    return map;
                })
                // Sắp xếp: Ưu tiên voucher giảm nhiều tiền nhất lên đầu
                .sorted((v1, v2) -> {
                    BigDecimal d1 = (BigDecimal) v1.get("discountAmount");
                    BigDecimal d2 = (BigDecimal) v2.get("discountAmount");
                    return d2.compareTo(d1);
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = "";
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            email = oauthToken.getPrincipal().getAttribute("email");
        } else {
            email = authentication.getName();
        }
        if (email != null && !email.isEmpty()) {
            return userService.findByEmail(email);
        }
        return null;
    }
}