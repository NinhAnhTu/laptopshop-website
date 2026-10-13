package com.example.laptopshop.controller.admin;

import com.example.laptopshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String panelRange,
            @RequestParam(required = false) String revenueYear,
            @RequestParam(required = false) String topProdRange,
            @RequestParam(required = false) String statusRange,
            Model model) {

        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        panelRange = (panelRange == null || panelRange.isEmpty()) ? todayStr : panelRange;
        topProdRange = (topProdRange == null || topProdRange.isEmpty()) ? todayStr : topProdRange;
        statusRange = (statusRange == null || statusRange.isEmpty()) ? todayStr : statusRange;
        revenueYear = (revenueYear == null || revenueYear.isEmpty()) ? String.valueOf(LocalDate.now().getYear()) : revenueYear;

        // 1. CARDS TỔNG QUAN
        model.addAttribute("totalOrders", dashboardService.countTotalOrders(panelRange));
        model.addAttribute("deliveredOrders", dashboardService.countDeliveredOrders(panelRange));
        model.addAttribute("cancelledOrders", dashboardService.countCancelledOrders(panelRange));
        model.addAttribute("totalRevenue", dashboardService.calculateTotalRevenue(panelRange));

        // 2. BIỂU ĐỒ DOANH THU (ĐỘNG)
        Map<String, Object> revenueChartData = dashboardService.getDynamicMonthlyRevenue(revenueYear);
        model.addAttribute("revLabels", revenueChartData.get("labels"));
        model.addAttribute("revData", revenueChartData.get("data"));

        // 3. TOP 5 SẢN PHẨM
        List<Object[]> topProducts = dashboardService.getTopSellingProducts(topProdRange);
        List<String> topNames = new ArrayList<>();
        List<Long> topSales = new ArrayList<>();
        if (topProducts != null) {
            for (Object[] row : topProducts) {
                topNames.add((String) row[0]);
                topSales.add((Long) row[1]);
            }
        }
        model.addAttribute("topNames", topNames);
        model.addAttribute("topSales", topSales);

        // 4. TRẠNG THÁI ĐƠN HÀNG
        List<Long> statusData = List.of(
                dashboardService.countPendingOrders(statusRange),
                dashboardService.countDeliveredOrders(statusRange),
                dashboardService.countCancelledOrders(statusRange)
        );
        model.addAttribute("statusData", statusData);

        // Giữ lại state cho UI
        model.addAttribute("panelRange", panelRange);
        model.addAttribute("revenueYear", revenueYear);
        model.addAttribute("topProdRange", topProdRange);
        model.addAttribute("statusRange", statusRange);
        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }
}