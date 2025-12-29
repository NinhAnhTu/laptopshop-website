package com.example.laptopshop.controller.admin;

import com.example.laptopshop.repository.OrderRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Object[]> revenueData = orderService.getMonthlyRevenue();
        List<Long> monthlyRevenue = new ArrayList<>(Collections.nCopies(12, 0L));

        if (revenueData != null) {
            for (Object[] row : revenueData) {
                if (row[0] != null && row[1] != null) {
                    int month = (int) row[0];
                    BigDecimal amount = (BigDecimal) row[1];
                    monthlyRevenue.set(month - 1, amount.longValue());
                }
            }
        }
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        // --- 2. DỮ LIỆU THỐNG KÊ TỔNG QUAN (CARD) ---
        long totalOrders = orderRepository.count();
        long deliveredOrders = orderRepository.countByStatus("Đã giao");
        long cancelledOrders = orderRepository.countByStatus("Đã hủy");
        long pendingOrders = orderRepository.countByStatus("Chờ xác nhận");

        // Tính tổng doanh thu thực tế (Tổng của biểu đồ)
        long totalRevenue = monthlyRevenue.stream().mapToLong(Long::longValue).sum();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        // --- 3. DỮ LIỆU BIỂU ĐỒ TRÒN: TRẠNG THÁI ĐƠN HÀNG ---
        // Thứ tự mảng: [Chờ xác nhận, Đã giao, Đã hủy]
        List<Long> orderStatusData = List.of(pendingOrders, deliveredOrders, cancelledOrders);
        model.addAttribute("orderStatusData", orderStatusData);

        // --- 4. DỮ LIỆU BIỂU ĐỒ NGANG: TOP 5 SẢN PHẨM BÁN CHẠY ---
        // Gọi hàm getTopSellingProductsData từ Repository
        List<Object[]> topProducts = productRepository.getTopSellingProductsData(PageRequest.of(0, 5));

        List<String> topProductNames = new ArrayList<>();
        List<Long> topProductSales = new ArrayList<>();

        if (topProducts != null) {
            for (Object[] row : topProducts) {
                topProductNames.add((String) row[0]); // Tên sản phẩm
                topProductSales.add((Long) row[1]);   // Số lượng bán
            }
        }
        model.addAttribute("topProductNames", topProductNames);
        model.addAttribute("topProductSales", topProductSales);

        // --- 5. CẤU HÌNH GIAO DIỆN ---
        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }
}