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
    public String updateAccount(@ModelAttribute("user") User user, @RequestParam(value = "from", required = false) String from,
                                RedirectAttributes redirectAttributes) {
        try {
            // Hàm này giờ chỉ làm nhiệm vụ cập nhật Tên và SĐT của Account gốc
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("failedUser", user);
        }
        // GIỮ NGUYÊN TAB PROFILE & TRẠNG THÁI FROM
        String redirectUrl = "redirect:/account?tab=profile";
        if (from != null && from.equals("checkout")) {
            redirectUrl += "&from=checkout";
        }
        return redirectUrl;
    }

    // --- 3. [SỬA LẠI] THÊM HOẶC CẬP NHẬT ĐỊA CHỈ ---
    @PostMapping("/account/add-address") // Đổi tên thành add-or-update-address nếu muốn rành mạch
    public String addOrUpdateAddress(@RequestParam(value = "addressId", required = false) Long addressId, // [MỚI] ID để biết là sửa hay thêm
                                     @RequestParam("receiverName") String receiverName,
                                     @RequestParam("receiverPhone") String receiverPhone,
                                     @RequestParam("ghnProvinceId") Integer ghnProvinceId,
                                     @RequestParam("ghnDistrictId") Integer ghnDistrictId,
                                     @RequestParam("ghnWardCode") String ghnWardCode,
                                     @RequestParam("fullAddress") String fullAddress,
                                     @RequestParam(value = "isDefault", defaultValue = "false") boolean isDefault, // [MỚI] Cho phép set mặc định
                                     @RequestParam(value = "from", required = false) String from,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {

        User user = getUserFromPrincipal(principal);
        if (user == null) return "redirect:/login";

        UserAddress addressToSave;

        // Nếu có ID -> Cập nhật địa chỉ cũ
        if (addressId != null) {
            addressToSave = userAddressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

            // Đảm bảo địa chỉ này đúng là của User đang đăng nhập
            if (!addressToSave.getUser().getUserId().equals(user.getUserId())) {
                return "redirect:/account";
            }
        } else {
            // Không có ID -> Tạo địa chỉ mới
            addressToSave = new UserAddress();
            addressToSave.setUser(user);
        }

        addressToSave.setReceiverName(receiverName);
        addressToSave.setReceiverPhone(receiverPhone);
        addressToSave.setGhnProvinceId(ghnProvinceId);
        addressToSave.setGhnDistrictId(ghnDistrictId);
        addressToSave.setGhnWardCode(ghnWardCode);
        addressToSave.setFullAddress(fullAddress);

        // Xử lý cờ Mặc định
        if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
            addressToSave.setIsDefault(true); // Địa chỉ đầu tiên luôn mặc định
        } else if (isDefault) {
            // Nếu user tick chọn Mặc định, phải gỡ bỏ Mặc định của các địa chỉ cũ
            for (UserAddress oldAddr : user.getAddresses()) {
                oldAddr.setIsDefault(false);
                userAddressRepository.save(oldAddr);
            }
            addressToSave.setIsDefault(true);
        }

        userAddressRepository.save(addressToSave);

        redirectAttributes.addFlashAttribute("successMessage",
                addressId != null ? "Cập nhật địa chỉ thành công!" : "Thêm địa chỉ nhận hàng mới thành công!");

        String redirectUrl = "redirect:/account?tab=profile";
        if (from != null && from.equals("checkout")) {
            redirectUrl += "&from=checkout";
        }
        return redirectUrl;
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

        // [BỔ SUNG] Đóng gói dữ liệu cũ để trả về HTML nếu lỗi
        redirectAttributes.addFlashAttribute("oldPwd", oldPassword);
        redirectAttributes.addFlashAttribute("newPwd", newPassword);
        redirectAttributes.addFlashAttribute("confirmPwd", confirmPassword);

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/account?tab=password"; // Dù lỗi cũng giữ ở tab mật khẩu
        }

        try {
            String email = principal.getName();
            if (principal instanceof OAuth2AuthenticationToken) {
                email = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttribute("email");
            }
            User currentUser = userService.findByEmail(email);

            userService.changeClientPassword(currentUser, oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Kiểm tra email để xem thông tin.");

            redirectAttributes.getFlashAttributes().remove("oldPwd");
            redirectAttributes.getFlashAttributes().remove("newPwd");
            redirectAttributes.getFlashAttributes().remove("confirmPwd");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/account?tab=password"; // Thành công thì giữ tab mật khẩu
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