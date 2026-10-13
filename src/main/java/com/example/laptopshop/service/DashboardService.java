package com.example.laptopshop.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    long countTotalOrders(String timeRange);
    long countDeliveredOrders(String timeRange);
    long countCancelledOrders(String timeRange);
    long countPendingOrders(String timeRange);
    long calculateTotalRevenue(String timeRange);

    Map<String, Object> getDynamicMonthlyRevenue(String year);
    List<Object[]> getTopSellingProducts(String timeRange);
}