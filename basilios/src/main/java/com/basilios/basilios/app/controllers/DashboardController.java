package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.core.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('FUNCIONARIO')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/revenue")
    public ResponseEntity<RevenueDTO> getRevenue(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        RevenueDTO dto = dashboardService.getRevenue(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/orders-period")
    public ResponseEntity<OrdersCountDTO> getOrdersCount(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        OrdersCountDTO dto = dashboardService.getOrdersCount(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/average-ticket")
    public ResponseEntity<AverageTicketDTO> getAverageTicket(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        AverageTicketDTO dto = dashboardService.getAverageTicket(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
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
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        CancellationRateDTO dto = dashboardService.getCancellationRate(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/average-delivery-time")
    public ResponseEntity<AverageDeliveryTimeDTO> getAverageDeliveryTime(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        AverageDeliveryTimeDTO dto = dashboardService.getAverageDeliveryTime(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/order-peaks")
    public ResponseEntity<OrderPeaksDTO> getOrderPeaks(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        OrderPeaksDTO dto = dashboardService.getOrderPeaks(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        List<TopProductDTO> dtos = dashboardService.getTopProductsByUnits(dtaInicio, dtaFim, limit);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/champion")
    public ResponseEntity<ChampionDTO> getChampion(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        Optional<ChampionDTO> champion = dashboardService.getChampionOfPeriod(dtaInicio, dtaFim);
        return champion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private LocalDateTime parseStart(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        LocalDate date = LocalDate.parse(dateStr);
        return date.atStartOfDay();
    }

    private LocalDateTime parseEnd(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        LocalDate date = LocalDate.parse(dateStr);
        return date.atTime(23, 59, 59);
    }
}
