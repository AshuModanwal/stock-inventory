package com.stockpilot.inventory.dto.dashboard;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    private Double totalRevenue;
    private Double todayRevenue;
    private Double monthRevenue;
    private Double totalProfit;
    private Long totalProducts;
    private Long totalStock;
    private Double inventoryValue;
    private Long totalSales;
    private Long todaySales;
    private Long totalCustomers;
    private Long lowStockCount;
    private Long outOfStockCount;
    private List<Map<String, Object>> revenueByDay;
    private List<Map<String, Object>> topProducts;
    private List<Map<String, Object>> revenueByCategory;
    private List<Map<String, Object>> topSalespeople;
    private List<Map<String, Object>> recentSales;
}
