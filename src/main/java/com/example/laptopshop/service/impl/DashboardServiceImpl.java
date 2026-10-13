package com.example.laptopshop.service.impl;

import com.example.laptopshop.repository.OrderRepository;
import com.example.laptopshop.repository.ProductRepository;
import com.example.laptopshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private LocalDateTime[] parseTimeRange(String timeRange) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        if (timeRange != null && timeRange.contains(" - ")) {
            String[] parts = timeRange.split(" - ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            start = LocalDate.parse(parts[0], formatter).atStartOfDay();
            end = LocalDate.parse(parts[1], formatter).atTime(LocalTime.MAX);
        } else {
            // Mặc định là Today
            start = LocalDate.now().atStartOfDay();
        }
        return new LocalDateTime[]{start, end};
    }

    @Override
    public long countTotalOrders(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        return orderRepository.countByCreatedAtBetween(range[0], range[1]);
    }

    @Override
    public long countDeliveredOrders(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        return orderRepository.countByStatusAndCreatedAtBetween("Đã giao", range[0], range[1]);
    }

    @Override
    public long countCancelledOrders(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        return orderRepository.countByStatusAndCreatedAtBetween("Đã hủy", range[0], range[1]);
    }

    @Override
    public long countPendingOrders(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        return orderRepository.countByStatusAndCreatedAtBetween("Chờ xác nhận", range[0], range[1]);
    }

    @Override
    public long calculateTotalRevenue(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        BigDecimal revenue = orderRepository.calculateRevenueBetween(range[0], range[1]);
        return revenue != null ? revenue.longValue() : 0L;
    }

    @Override
    public Map<String, Object> getDynamicMonthlyRevenue(String year) {
        int targetYear = (year == null || year.isEmpty()) ? LocalDate.now().getYear() : Integer.parseInt(year);
        List<Object[]> revenueData = orderRepository.getMonthlyRevenueByYear(targetYear);

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        if (revenueData != null) {
            for (Object[] row : revenueData) {
                if (row[0] != null && row[1] != null) {
                    labels.add("Tháng " + row[0]);
                    data.add(((BigDecimal) row[1]).longValue());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

    @Override
    public List<Object[]> getTopSellingProducts(String timeRange) {
        LocalDateTime[] range = parseTimeRange(timeRange);
        return productRepository.getTopSellingProductsDataBetween(range[0], range[1], PageRequest.of(0, 5));
    }
}