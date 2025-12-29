package com.example.laptopshop.controller.admin;

import com.example.laptopshop.entity.Order;
import com.example.laptopshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String date) {

        // Gọi hàm tìm kiếm
        List<Order> orders = orderService.searchOrders(keyword, status, date);
        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("date", date);

        // Active sidebar
        model.addAttribute("activePage", "orders");

        return "admin/order/list";
    }

    // 2. Xem chi tiết đơn hàng
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/admin/orders";

        model.addAttribute("order", order);
        model.addAttribute("activePage", "orders");
        return "admin/order/detail";
    }

//    // 3. Cập nhật trạng thái (POST từ form chi tiết)
//    @PostMapping("/update-status")
//    public String updateStatus(@RequestParam("id") Long id,
//                               @RequestParam("status") String status) {
//        orderService.updateOrderStatus(id, status);
//        return "redirect:/admin/orders/view/" + id;
//    }
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {

        orderService.updateOrderStatus(id, status);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");

        return "redirect:/admin/orders/view/" + id;
    }
}