package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.core.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas e relatórios agregados")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Helper to parse incoming date/time strings. Accepts ISO date or datetime.
    private LocalDateTime parseStart(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            // try date only
            LocalDate d = LocalDate.parse(value);
            return LocalDateTime.of(d, LocalTime.MIN);
        }
    }

    private LocalDateTime parseEnd(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            // try date only
            LocalDate d = LocalDate.parse(value);
            return LocalDateTime.of(d, LocalTime.MAX);
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueDTO> getRevenue(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getRevenueDTO(start, end));
    }

    @GetMapping("/orders")
    public ResponseEntity<OrdersCountDTO> getOrdersCount(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getOrdersCountDTO(start, end));
    }

    @GetMapping("/average-ticket")
    public ResponseEntity<AverageTicketDTO> getAverageTicket(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getAverageTicketDTO(start, end));
    }

    @GetMapping("/items-sold")
    public ResponseEntity<ItemsSoldDTO> getItemsSold(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        long itemsSold = dashboardService.getItemsSold(start, end);
        return ResponseEntity.ok(ItemsSoldDTO.toResponse(itemsSold));
    }

    @GetMapping("/cancellation-rate")
    public ResponseEntity<CancellationRateDTO> getCancellationRate(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        long total = dashboardService.getOrdersCount(start, end);
        long cancelled = dashboardService.getCancelledOrdersCount(start, end);
        double rate = total == 0 ? 0.0 : ((double) cancelled / (double) total) * 100.0;
        return ResponseEntity.ok(CancellationRateDTO.toResponse(rate));
    }

    @GetMapping("/average-delivery-time")
    public ResponseEntity<AverageDeliveryTimeDTO> getAverageDeliveryTime(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getAverageDeliveryTimeDTO(start, end));
    }

    @GetMapping("/order-peaks")
    public ResponseEntity<OrderPeaksDTO> getOrderPeaks(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getOrderPeaksDTO(start, end));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getTopProductsByUnitsDTO(start, end, limit));
    }

    @GetMapping("/champion")
    public ResponseEntity<ChampionDTO> getChampion(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Optional<ChampionDTO> opt = dashboardService.getChampionOfPeriodDTO(start, end);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(null));
    }

}
