package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.core.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getRevenue(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/orders-period")
    public OrdersCountDTO getOrdersCount(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getOrdersCount(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/average-ticket")
    public AverageTicketDTO getAverageTicket(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getAverageTicket(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/items-sold")
    public ItemsSoldDTO getItemsSold(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getItemsSoldData(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/cancellation-rate")
    public CancellationRateDTO getCancellationRate(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getCancellationRate(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/average-delivery-time")
    public AverageDeliveryTimeDTO getAverageDeliveryTime(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getAverageDeliveryTime(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/order-peaks")
    public OrderPeaksDTO getOrderPeaks(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        return dashboardService.getOrderPeaks(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
    }

    @GetMapping("/top-products")
    public List<TopProductDTO> getTopProducts(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return dashboardService.getTopProductsByUnits(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null,
            limit
        );
    }

    @GetMapping("/champion")
    public ChampionDTO getChampion(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        Optional<ChampionDTO> champion = dashboardService.getChampionOfPeriod(
            dtaInicio != null ? dtaInicio.atStartOfDay() : null,
            dtaFim != null ? dtaFim.atTime(23, 59, 59, 999_999_999) : null
        );
        return champion.orElse(null);
    }
}
