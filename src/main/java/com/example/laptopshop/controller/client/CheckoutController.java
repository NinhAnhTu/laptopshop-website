package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.*;
import com.example.laptopshop.repository.UserAddressRepository;
import com.example.laptopshop.service.CartService;
import com.example.laptopshop.service.GhnApiService;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.UserService;
import com.example.laptopshop.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final VnPayService vnPayService;

    // [MỚI] Tiêm 2 Service mới vào
    private final UserAddressRepository userAddressRepository;
    private final GhnApiService ghnApiService;

    // --- 1. HIỂN THỊ TRANG THANH TOÁN ---
    @GetMapping("/checkout")
    public String showCheckoutPage(@RequestParam(value = "selectedItems", required = false) List<Long> selectedItems,
                                   Model model,
                                   Authentication authentication) {

        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        if (selectedItems == null || selectedItems.isEmpty()) {
            return "redirect:/cart?error=no_items_selected";
        }

        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getCartDetails().isEmpty()) {
            return "redirect:/cart";
        }

        List<CartDetail> checkoutItems = new ArrayList<>();
        double totalPrice = 0;

        for (CartDetail item : cart.getCartDetails()) {
            if (selectedItems.contains(item.getCartDetailId())) {
                checkoutItems.add(item);
                totalPrice += item.getProduct().getSalePrice().doubleValue() * item.getQuantity();
            }
        }

        if (checkoutItems.isEmpty()) return "redirect:/cart";

        // [MỚI] XỬ LÝ SỔ ĐỊA CHỈ & TÍNH PHÍ SHIP MẶC ĐỊNH
        BigDecimal shippingFee = BigDecimal.ZERO;
        List<UserAddress> addresses = user.getAddresses();

        if (addresses != null && !addresses.isEmpty()) {
            // Tìm địa chỉ mặc định, nếu không có thì lấy cái đầu tiên
            UserAddress defaultAddress = addresses.stream()
                    .filter(UserAddress::getIsDefault)
                    .findFirst()
                    .orElse(addresses.get(0));

            // Gọi API GHN tính phí ship cho địa chỉ mặc định
            if (defaultAddress.getGhnDistrictId() != null && defaultAddress.getGhnWardCode() != null) {
                int fee = ghnApiService.calculateShippingFee(defaultAddress.getGhnDistrictId(), defaultAddress.getGhnWardCode());
                shippingFee = BigDecimal.valueOf(fee);
            }
        }

        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("cartItems", checkoutItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);
        model.addAttribute("selectedIds", selectedItems);

        return "client/checkout";
    }

    // --- 2. XỬ LÝ ĐẶT HÀNG ---
    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("selectedAddressId") Long selectedAddressId, // [MỚI] Chỉ cần nhận ID địa chỉ
                             @RequestParam(value = "note", required = false) String note,
                             @RequestParam(value = "paymentMethod", defaultValue = "1") Long paymentMethodId,
                             @RequestParam("selectedIds") List<Long> selectedIds,
                             @RequestParam(value = "voucherCode", required = false) String voucherCode,
                             Authentication authentication,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = getUserFromAuthentication(authentication);
        if (user == null) return "redirect:/login";

        try {
            // 1. Lấy thông tin địa chỉ từ Database
            UserAddress address = userAddressRepository.findById(selectedAddressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng!"));

            // 2. Tạo đơn hàng (Đã đổi tham số truyền vào thành dữ liệu của Sổ địa chỉ)
            Order order = orderService.placeOrder(
                    user, note,
                    address.getFullAddress(), address.getReceiverName(), address.getReceiverPhone(),
                    address.getGhnDistrictId(), address.getGhnWardCode(), // Truyền 2 mã GHN thay vì CityId
                    paymentMethodId, selectedIds, voucherCode
            );

            // 3. Xử lý VNPAY
            if (paymentMethodId == 2) {
                orderService.updateOrderStatus(order.getOrderId(), "Chờ thanh toán");
                String vnpayUrl = vnPayService.createVnPayPayment(request, order);
                return "redirect:" + vnpayUrl;
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }

        return "redirect:/checkout/success";
    }

    // --- 3. [MỚI] API AJAX TÍNH LẠI PHÍ SHIP KHI ĐỔI ĐỊA CHỈ TRÊN GIAO DIỆN ---
    @GetMapping("/api/shipping/calculate-fee")
    @ResponseBody
    public Integer calculateFee(@RequestParam("districtId") Integer districtId,
                                @RequestParam("wardCode") String wardCode) {
        return ghnApiService.calculateShippingFee(districtId, wardCode);
    }

    // --- CÁC HÀM XỬ LÝ VNPAY VÀ LẤY USER GIỮ NGUYÊN NHƯ CŨ ---
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
            orderService.saveTransaction(Long.parseLong(orderId), totalPrice, transactionId);
            return "client/order_success";
        } else {
            orderService.cancelOrder(Long.parseLong(orderId));
            return "client/order_fail";
        }
    }

    @GetMapping("/checkout/success")
    public String orderSuccess() {
        return "client/order_success";
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String email = authentication instanceof OAuth2AuthenticationToken ?
                ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttribute("email") :
                authentication.getName();
        return email != null && !email.isEmpty() ? userService.findByEmail(email) : null;
    }
}