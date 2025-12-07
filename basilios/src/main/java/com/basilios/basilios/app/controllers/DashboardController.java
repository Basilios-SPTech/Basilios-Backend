package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.core.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public RevenueDTO getRevenue(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getRevenue(dtaInicio, dtaFim);
    }

    @GetMapping("/orders")
    public OrdersCountDTO getOrdersCount(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getOrdersCount(dtaInicio, dtaFim);
    }

    @GetMapping("/average-ticket")
    public AverageTicketDTO getAverageTicket(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getAverageTicket(dtaInicio, dtaFim);
    }

    @GetMapping("/items-sold")
    public ItemsSoldDTO getItemsSold(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getItemsSoldData(dtaInicio, dtaFim);
    }

    @GetMapping("/cancellation-rate")
    public CancellationRateDTO getCancellationRate(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getCancellationRate(dtaInicio, dtaFim);
    }

    @GetMapping("/average-delivery-time")
    public AverageDeliveryTimeDTO getAverageDeliveryTime(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getAverageDeliveryTime(dtaInicio, dtaFim);
    }

    @GetMapping("/order-peaks")
    public OrderPeaksDTO getOrderPeaks(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        return dashboardService.getOrderPeaks(dtaInicio, dtaFim);
    }

    @GetMapping("/top-products")
    public List<TopProductDTO> getTopProducts(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return dashboardService.getTopProductsByUnits(dtaInicio, dtaFim, limit);
    }

    @GetMapping("/champion")
    public ChampionDTO getChampion(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dtaFim) {
        Optional<ChampionDTO> champion = dashboardService.getChampionOfPeriod(dtaInicio, dtaFim);
        return champion.orElse(null);
    }
}
