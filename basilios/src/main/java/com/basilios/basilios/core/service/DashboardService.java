package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.infra.repository.ProductOrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductService productService;

    /**
     * Visão geral combinando estatísticas de produtos e vendas
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> productStats = productService.getStatistics();
        Object[] salesStats = productOrderRepository.getSalesStatistics();

        Map<String, Object> overview = new HashMap<>();
        overview.put("products", productStats);

        Map<String, Object> sales = new HashMap<>();
        if (salesStats != null) {
            sales.put("totalItems", salesStats[0] != null ? ((Number) salesStats[0]).longValue() : 0L);
            sales.put("totalQuantity", salesStats[1] != null ? ((Number) salesStats[1]).longValue() : 0L);
            sales.put("totalRevenue", salesStats[2] != null ? (BigDecimal) salesStats[2] : BigDecimal.ZERO);
            sales.put("avgUnitPrice", salesStats[3] != null ? (BigDecimal) salesStats[3] : BigDecimal.ZERO);
            sales.put("distinctProductsSold", salesStats[4] != null ? ((Number) salesStats[4]).longValue() : 0L);
        } else {
            sales.put("totalItems", 0L);
            sales.put("totalQuantity", 0L);
            sales.put("totalRevenue", BigDecimal.ZERO);
            sales.put("avgUnitPrice", BigDecimal.ZERO);
            sales.put("distinctProductsSold", 0L);
        }

        overview.put("sales", sales);
        return overview;
    }

    /**
     * Produtos mais vendidos (delegates to ProductService which já monta estruturas)
     */
    public List<Map<String, Object>> getBestSellers(int limit) {
        return productService.getBestSellers(limit);
    }

    /**
     * Receita diária dos últimos N dias. Retorna lista ordenada por data asc.
     */
    public List<Map<String, Object>> getRevenueLastDays(int days) {
        if (days <= 0) days = 30;

        List<Map<String, Object>> series = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Object[] stats = productOrderRepository.getSalesStatisticsByPeriod(start, end);
            BigDecimal revenue = BigDecimal.ZERO;
            if (stats != null && stats.length >= 3 && stats[2] != null) {
                revenue = (BigDecimal) stats[2];
            }

            series.add(Map.of(
                    "date", date.toString(),
                    "revenue", revenue
            ));
        }

        return series;
    }

    /**
     * Produtos que nunca foram vendidos (delegates to ProductService)
     */
    public List<ProductResponseDTO> getNeverSoldProducts() {
        return productService.getNeverSoldProducts();
    }

    /**
     * Produtos sem ingredientes cadastrados (delegates to ProductService)
     */
    public List<ProductResponseDTO> getProductsWithoutIngredients() {
        return productService.getProductsWithoutIngredients();
    }

    /**
     * Estatísticas de preço (média/min/max)
     */
    public Map<String, Object> getPriceAnalysis() {
        Map<String, Object> res = new HashMap<>();
        Map<String, BigDecimal> stats = productService.getPriceAnalysis();
        res.put("average", stats.getOrDefault("average", BigDecimal.ZERO));
        res.put("min", stats.getOrDefault("min", BigDecimal.ZERO));
        res.put("max", stats.getOrDefault("max", BigDecimal.ZERO));
        return res;
    }
}

