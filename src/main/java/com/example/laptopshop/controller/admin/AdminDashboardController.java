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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @GetMapping("/dashboard/export")
    public void exportDashboardReport(
            @RequestParam(required = false) String panelRange,
            HttpServletResponse response) throws IOException {

        // 1. Chuẩn bị khoảng thời gian (giống hàm dashboard)
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        panelRange = (panelRange == null || panelRange.isEmpty()) ? todayStr : panelRange;

        // 2. Lấy dữ liệu từ Service
        long totalOrders = dashboardService.countTotalOrders(panelRange);
        long deliveredOrders = dashboardService.countDeliveredOrders(panelRange);
        long cancelledOrders = dashboardService.countCancelledOrders(panelRange);
        long totalRevenue = dashboardService.calculateTotalRevenue(panelRange);
        List<Object[]> topProducts = dashboardService.getTopSellingProducts(panelRange);

        // 3. Cấu hình Response để trình duyệt hiểu là file tải về
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Bao_Cao_Doanh_Thu.xlsx");

        // 4. Tạo file Excel bằng Apache POI
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo Cáo Tổng Quan");

            // Tạo Style in đậm cho Tiêu đề
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Ghi dòng 1: Thời gian báo cáo
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Thời gian báo cáo:");
            row0.createCell(1).setCellValue(panelRange);

            // Ghi bảng Chỉ số Tổng quan
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Chỉ số");
            row2.createCell(1).setCellValue("Giá trị");
            row2.getCell(0).setCellStyle(headerStyle);
            row2.getCell(1).setCellStyle(headerStyle);

            sheet.createRow(3).createCell(0).setCellValue("Tổng doanh thu (VNĐ)");
            sheet.getRow(3).createCell(1).setCellValue(totalRevenue);

            sheet.createRow(4).createCell(0).setCellValue("Tổng số đơn hàng");
            sheet.getRow(4).createCell(1).setCellValue(totalOrders);

            sheet.createRow(5).createCell(0).setCellValue("Đơn thành công");
            sheet.getRow(5).createCell(1).setCellValue(deliveredOrders);

            sheet.createRow(6).createCell(0).setCellValue("Đơn đã hủy");
            sheet.getRow(6).createCell(1).setCellValue(cancelledOrders);

            // Ghi bảng Top 5 Sản phẩm bán chạy
            Row row8 = sheet.createRow(8);
            row8.createCell(0).setCellValue("Top 5 Sản phẩm bán chạy nhất");
            row8.getCell(0).setCellStyle(headerStyle);

            Row row9 = sheet.createRow(9);
            row9.createCell(0).setCellValue("Tên sản phẩm");
            row9.createCell(1).setCellValue("Số lượng đã bán");
            row9.getCell(0).setCellStyle(headerStyle);
            row9.getCell(1).setCellStyle(headerStyle);

            int rowIdx = 10;
            if (topProducts != null) {
                for (Object[] prod : topProducts) {
                    Row r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue((String) prod[0]);
                    r.createCell(1).setCellValue(((Long) prod[1]).doubleValue()); // Cần parse thành số để Excel hiểu
                }
            }

            // Tự động căn chỉnh độ rộng 2 cột cho đẹp
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Đẩy dữ liệu file ra response
            workbook.write(response.getOutputStream());
        }
    }
}