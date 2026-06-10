package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.OrderDetail;
import com.example.laptopshop.entity.ProductSerial;
import com.example.laptopshop.entity.enums.SerialStatus;
import com.example.laptopshop.repository.ProductSerialRepository;
import com.example.laptopshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // --- THÊM REPO ĐỂ XỬ LÝ SERIAL ---
    private final ProductSerialRepository productSerialRepository;

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String date) {

        List<Order> orders = orderService.searchOrders(keyword, status, date);
        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("date", date);
        model.addAttribute("activePage", "orders");

        return "admin/order/list";
    }

    // --- SỬA LẠI HÀM VIEW ĐỂ LOAD DỮ LIỆU SERIAL ---
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/admin/orders";

        // 1. Lấy danh sách serial ĐÃ GÁN cho đơn hàng này (Gom nhóm theo Product ID)
        List<ProductSerial> allAssigned = productSerialRepository.findByOrderOrderId(id);
        Map<Long, List<ProductSerial>> assignedMap = allAssigned.stream()
                .collect(Collectors.groupingBy(ps -> ps.getProduct().getProductId()));

        // 2. Lấy danh sách serial CÒN TRỐNG (AVAILABLE) để Admin chọn
        Map<Long, List<ProductSerial>> availableMap = new HashMap<>();
        for (OrderDetail detail : order.getOrderDetails()) {
            Long pId = detail.getProduct().getProductId();
            List<ProductSerial> avails = productSerialRepository.findByProductProductIdAndStatus(pId, SerialStatus.AVAILABLE);
            availableMap.put(pId, avails);
        }

        model.addAttribute("order", order);
        model.addAttribute("assignedSerials", assignedMap); // Đẩy xuống View
        model.addAttribute("availableSerials", availableMap); // Đẩy xuống View
        model.addAttribute("activePage", "orders");

        return "admin/order/detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {

        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/orders/view/" + id;
    }

    // --- API MỚI 1: GÁN SERIAL CHO ĐƠN HÀNG ---
    @PostMapping("/assign-serial")
    public String assignSerial(@RequestParam Long orderId,
                               @RequestParam Long productId,
                               @RequestParam String serialNumber,
                               RedirectAttributes redirectAttributes) {

        // THÊM .orElse(null) Ở ĐÂY ĐỂ XỬ LÝ OPTIONAL
        ProductSerial serial = productSerialRepository.findBySerialNumber(serialNumber).orElse(null);

        Order order = orderService.getOrderById(orderId);

        if (serial != null && order != null && serial.getStatus() == SerialStatus.AVAILABLE) {
            serial.setOrder(order);

            // Sửa 2: Đổi từ SerialStatus.AVAILABLE thành SerialStatus.SOLD (Vì máy đã được bán)
            serial.setStatus(SerialStatus.SOLD);

            productSerialRepository.save(serial);
            redirectAttributes.addFlashAttribute("successMessage", "Gán Serial [" + serialNumber + "] thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Serial không hợp lệ hoặc đã được gán!");
        }
        return "redirect:/admin/orders/view/" + orderId;
    }

    // --- API MỚI 2: XÓA SERIAL KHỎI ĐƠN HÀNG (TRẢ LẠI KHO) ---
    @PostMapping("/remove-serial")
    public String removeSerial(@RequestParam Long orderId,
                               @RequestParam Long serialId,
                               RedirectAttributes redirectAttributes) {

        ProductSerial serial = productSerialRepository.findById(serialId).orElse(null);
        if (serial != null && serial.getOrder() != null && serial.getOrder().getOrderId().equals(orderId)) {
            serial.setOrder(null); // Gỡ khỏi đơn hàng
            serial.setStatus(SerialStatus.AVAILABLE); // Trả lại kho
            productSerialRepository.save(serial);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ Serial [" + serial.getSerialNumber() + "] trả lại kho!");
        }
        return "redirect:/admin/orders/view/" + orderId;
    }
}