package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.dashboard.DashboardResponse;
import com.stockpilot.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "#companyId")
    public DashboardResponse getDashboard(Long companyId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        Double totalRevenue = saleRepository.getTotalRevenue(companyId);
        Double todayRevenue = saleRepository.getRevenueByDateRange(companyId, todayStart, todayEnd);
        Double monthRevenue = saleRepository.getRevenueByDateRange(companyId, monthStart, todayEnd);

        Long totalProducts = productRepository.countByCompanyId(companyId);
        Long totalStock = productRepository.getTotalStockCount(companyId);
        Double inventoryValue = productRepository.getTotalInventoryValue(companyId);
        Long totalSales = saleRepository.countByCompanyId(companyId);
        Long todaySales = saleRepository.countByCompanyAndDateRange(companyId, todayStart, todayEnd);
        Long totalCustomers = customerRepository.countByCompanyId(companyId);
        Long lowStockCount = (long) productRepository.findLowStockByCompany(companyId).size();
        Long outOfStockCount = (long) productRepository.findOutOfStockByCompany(companyId).size();

        // Top products (last 30 days)
        List<Map<String, Object>> topProducts = saleItemRepository.getTopProducts(companyId, last30Days, todayEnd)
                .stream().limit(10).map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", row[0]); m.put("productName", row[1]);
                    m.put("quantitySold", row[2]); m.put("revenue", row[3]);
                    return m;
                }).toList();

        // Revenue by category
        List<Map<String, Object>> revenueByCategory = saleItemRepository.getRevenueByCategory(companyId, last30Days, todayEnd)
                .stream().map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("category", row[0]); m.put("revenue", row[1]);
                    return m;
                }).toList();

        // Top salespeople
        List<Map<String, Object>> topSalespeople = saleRepository.getTopSalespeople(companyId, last30Days, todayEnd)
                .stream().limit(5).map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", row[0]); m.put("name", row[1]);
                    m.put("salesCount", row[2]); m.put("revenue", row[3]);
                    return m;
                }).toList();

        // Revenue by day (last 7 days)
        List<Map<String, Object>> revenueByDay = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Double dayRevenue = saleRepository.getRevenueByDateRange(
                    companyId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", date.toString());
            m.put("revenue", dayRevenue != null ? dayRevenue : 0);
            revenueByDay.add(m);
        }

        return DashboardResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0)
                .todayRevenue(todayRevenue != null ? todayRevenue : 0)
                .monthRevenue(monthRevenue != null ? monthRevenue : 0)
                .totalProducts(totalProducts)
                .totalStock(totalStock != null ? totalStock : 0)
                .inventoryValue(inventoryValue != null ? inventoryValue : 0)
                .totalSales(totalSales)
                .todaySales(todaySales)
                .totalCustomers(totalCustomers)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .revenueByDay(revenueByDay)
                .topProducts(topProducts)
                .revenueByCategory(revenueByCategory)
                .topSalespeople(topSalespeople)
                .build();
    }
}
