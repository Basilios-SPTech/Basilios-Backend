package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.core.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('FUNCIONARIO')")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Métricas e indicadores do negócio")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue")
    @Operation(summary = "Receita do período", description = "Retorna a receita total no período informado")
    public ResponseEntity<RevenueDTO> getRevenue(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        RevenueDTO dto = dashboardService.getRevenue(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/orders-period")
    @Operation(summary = "Total de pedidos no período")
    public ResponseEntity<OrdersCountDTO> getOrdersCount(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        OrdersCountDTO dto = dashboardService.getOrdersCount(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/average-ticket")
    @Operation(summary = "Ticket médio do período")
    public ResponseEntity<AverageTicketDTO> getAverageTicket(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        AverageTicketDTO dto = dashboardService.getAverageTicket(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/items-sold")
    @Operation(summary = "Itens vendidos no período")
    public ResponseEntity<ItemsSoldDTO> getItemsSold(
            @RequestParam(name = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(name = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        long itemsSold = dashboardService.getItemsSold(dtaInicio, dtaFim);
        return ResponseEntity.ok(ItemsSoldDTO.toResponse(itemsSold));
    }

    @GetMapping("/cancellation-rate")
    @Operation(summary = "Taxa de cancelamento")
    public ResponseEntity<CancellationRateDTO> getCancellationRate(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        CancellationRateDTO dto = dashboardService.getCancellationRate(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/average-delivery-time")
    @Operation(summary = "Tempo médio de entrega")
    public ResponseEntity<AverageDeliveryTimeDTO> getAverageDeliveryTime(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        AverageDeliveryTimeDTO dto = dashboardService.getAverageDeliveryTime(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/order-peaks")
    @Operation(summary = "Picos de pedidos")
    public ResponseEntity<OrderPeaksDTO> getOrderPeaks(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        OrderPeaksDTO dto = dashboardService.getOrderPeaks(dtaInicio, dtaFim);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/top-products")
    @Operation(summary = "Produtos mais vendidos")
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
    @Operation(summary = "Campeão de vendas do período")
    public ResponseEntity<ChampionDTO> getChampion(
            @RequestParam(value = "dta_inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaInicio,
            @RequestParam(value = "dta_fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaFim) {
        Optional<ChampionDTO> champion = dashboardService.getChampionOfPeriod(dtaInicio, dtaFim);
        return champion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
