package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.UserAddress;
import com.example.laptopshop.entity.Warranty;
import com.example.laptopshop.repository.UserAddressRepository;
import com.example.laptopshop.service.OrderService;
import com.example.laptopshop.service.UserService;
import com.example.laptopshop.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientController {

    private final UserService userService;
    private final OrderService orderService;
    private final WarrantyService warrantyService;
    private final UserAddressRepository userAddressRepository;

    // --- 1. HIỂN THỊ TRANG TÀI KHOẢN ---
    @GetMapping("/account")
    public String myAccount(Model model, Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return "redirect:/login";

        List<Order> orders = orderService.getOrdersByUser(user);
        if (orders == null) orders = Collections.emptyList();

        List<Warranty> warranties = warrantyService.getWarrantiesByUser(user);
        if (warranties == null) warranties = Collections.emptyList();

        // Đã xóa phần load Cities cũ vì Frontend giờ gọi trực tiếp từ API của GHN
        model.addAttribute("warranties", warranties);
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "client/account";
    }

    // --- 2. CẬP NHẬT THÔNG TIN CÁ NHÂN (CƠ BẢN) ---
    @PostMapping("/account/update")
    public String updateAccount(@ModelAttribute("user") User user,
                                RedirectAttributes redirectAttributes) {
        try {
            // Hàm này giờ chỉ làm nhiệm vụ cập nhật Tên và SĐT của Account gốc
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account";
    }

    // --- 3. [MỚI] THÊM ĐỊA CHỈ GIAO HÀNG VÀO SỔ ĐỊA CHỈ ---
    @PostMapping("/account/add-address")
    public String addAddress(@RequestParam("receiverName") String receiverName,
                             @RequestParam("receiverPhone") String receiverPhone,
                             @RequestParam("ghnProvinceId") Integer ghnProvinceId,
                             @RequestParam("ghnDistrictId") Integer ghnDistrictId,
                             @RequestParam("ghnWardCode") String ghnWardCode,
                             @RequestParam("fullAddress") String fullAddress,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        User user = getUserFromPrincipal(principal);
        if (user == null) return "redirect:/login";

        UserAddress newAddress = new UserAddress();
        newAddress.setUser(user);
        newAddress.setReceiverName(receiverName);
        newAddress.setReceiverPhone(receiverPhone);
        newAddress.setGhnProvinceId(ghnProvinceId);
        newAddress.setGhnDistrictId(ghnDistrictId);
        newAddress.setGhnWardCode(ghnWardCode);
        newAddress.setFullAddress(fullAddress);

        // Nếu đây là địa chỉ đầu tiên được thêm vào, tự động thiết lập làm Mặc định
        if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
            newAddress.setIsDefault(true);
        } else {
            newAddress.setIsDefault(false);
        }

        userAddressRepository.save(newAddress);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ nhận hàng mới thành công!");
        return "redirect:/account";
    }

    // --- 4. HỦY ĐƠN HÀNG ---
    @GetMapping("/account/cancel-order/{id}")
    public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        orderService.cancelOrder(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " thành công!");
        return "redirect:/account";
    }

    // --- 4. ĐỔI MẬT KHẨU ---
    @PostMapping("/account/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/account";
        }

        try {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
            }
            User currentUser = userService.findByEmail(email);

            userService.changeClientPassword(currentUser, oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Kiểm tra email để xem thông tin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/account";
    }
    // --- XEM CHI TIẾT ĐƠN HÀNG ---
    @GetMapping("/account/order/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Order order = orderService.getOrderById(id);

        // Chặn không cho xem trộm đơn hàng của người khác
        String currentEmail = principal.getName();
        if (principal instanceof OAuth2AuthenticationToken) {
            currentEmail = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
        }
        User currentUser = userService.findByEmail(currentEmail);

        if (order == null || !order.getUser().getUserId().equals(currentUser.getUserId())) {
            return "redirect:/account";
        }

        model.addAttribute("order", order);
        return "client/order_detail"; // Trỏ tới file HTML mới
    }
    // --- HÀM TIỆN ÍCH DÙNG CHUNG: LẤY USER ĐANG ĐĂNG NHẬP ---
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) return null;

        String email = principal.getName();
        // Xử lý trường hợp đăng nhập bằng Google (OAuth2)
        if (principal instanceof OAuth2AuthenticationToken) {
            email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
        }
        return userService.findByEmail(email);
    }

}