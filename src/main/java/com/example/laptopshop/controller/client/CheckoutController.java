package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Cart;
import com.example.laptopshop.entity.CartDetail;
import com.example.laptopshop.entity.City;
import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.repository.CityRepository;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.UserService;
import com.example.laptopshop.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final CityRepository cityRepository;
    private final VnPayService vnPayService;

    // 1. Hiển thị trang thanh toán
    @GetMapping("/checkout")
    public String showCheckoutPage(@RequestParam(value = "selectedItems", required = false) List<Long> selectedItems,
                                   Model model,
                                   Authentication authentication) { // Dùng Authentication thay vì Principal

        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        // Kiểm tra nếu không có sản phẩm nào được chọn (từ giỏ hàng truyền sang)
        if (selectedItems == null || selectedItems.isEmpty()) {
            return "redirect:/cart?error=no_items_selected";
        }

        Cart cart = cartService.getCartByUser(user);

        if (cart == null || cart.getCartDetails().isEmpty()) {
            return "redirect:/cart";
        }

        // Lọc ra các sản phẩm được chọn để hiển thị và tính tiền
        List<CartDetail> checkoutItems = new ArrayList<>();
        double totalPrice = 0;

        for (CartDetail item : cart.getCartDetails()) {
            if (selectedItems.contains(item.getCartDetailId())) {
                checkoutItems.add(item);
                totalPrice += item.getProduct().getSalePrice().doubleValue() * item.getQuantity();
            }
        }

        // Nếu lọc xong mà danh sách rỗng (hack url) thì đá về giỏ hàng
        if (checkoutItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<City> cities = cityRepository.findAll();

        model.addAttribute("cartItems", checkoutItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cities", cities);
        model.addAttribute("user", user);

        // Truyền danh sách ID này sang View để lát nữa form POST gửi lại
        model.addAttribute("selectedIds", selectedItems);

        return "client/checkout";
    }

    // 2. Xử lý đặt hàng
    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("fullname") String fullname,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             @RequestParam("cityId") Long cityId,
                             @RequestParam(value = "note", required = false) String note,
                             @RequestParam(value = "paymentMethod", defaultValue = "1") Long paymentMethodId,
                             @RequestParam("selectedIds") List<Long> selectedIds,
                             @RequestParam(value = "voucherCode", required = false) String voucherCode,
                             Authentication authentication, // [SỬA] Dùng Authentication
                             HttpServletRequest request) {

        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        try {
            // Bước 1: Tạo đơn hàng
            Order order = orderService.placeOrder(user, note, address, fullname, phone, cityId, paymentMethodId, selectedIds, voucherCode);

            // Bước 2: Kiểm tra phương thức thanh toán
            // Nếu là VNPAY (ID = 2)
            if (paymentMethodId == 2) {
                // Tạm thời set trạng thái chờ thanh toán
                orderService.updateOrderStatus(order.getOrderId(), "Chờ thanh toán");
                // Tạo URL thanh toán và chuyển hướng
                String vnpayUrl = vnPayService.createVnPayPayment(request, order);
                return "redirect:" + vnpayUrl;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=true";
        }

        return "redirect:/checkout/success";
    }

    // 3. Xử lý kết quả trả về từ VNPAY
    @GetMapping("/vnpay-payment-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String orderId = request.getParameter("vnp_TxnRef");

        model.addAttribute("orderId", orderId);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        if (paymentStatus == 1) {
            orderService.updateOrderStatus(Long.parseLong(orderId), "Đã thanh toán");
            return "client/order_success";
        } else {
            // Thanh toán thất bại hoặc hủy bỏ
            orderService.cancelOrder(Long.parseLong(orderId));
            return "client/order_fail";
        }
    }

    @GetMapping("/checkout/success")
    public String orderSuccess() {
        return "client/order_success";
    }

    // --- LẤY USER CHUẨN ---
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