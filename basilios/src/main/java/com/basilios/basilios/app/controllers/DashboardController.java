package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas e relatórios agregados")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

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
    @Operation(summary = "Receita (período)", description = "Soma total do valor (dinheiro) de todos os pedidos realizados no período")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Map<String, Object> res = Map.of("revenue", dashboardService.getRevenue(start, end));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/orders")
    @Operation(summary = "Quantidade de pedidos (período)", description = "Quantidade total de pedidos realizados no período")
    public ResponseEntity<Map<String, Object>> getOrdersCount(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Map<String, Object> res = Map.of("orders", dashboardService.getOrdersCount(start, end));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/average-ticket")
    @Operation(summary = "Ticket médio (período)", description = "Valor da Receita total dividido pela quantidade de Pedidos")
    public ResponseEntity<Map<String, Object>> getAverageTicket(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Map<String, Object> res = Map.of("averageTicket", dashboardService.getAverageTicket(start, end));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/items-sold")
    @Operation(summary = "Itens vendidos (período)", description = "Quantidade total de itens vendidos dentro do período")
    public ResponseEntity<Map<String, Object>> getItemsSold(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Map<String, Long> stats = dashboardService.getItemsSoldAndNotSold(start, end);
        Map<String, Object> res = new HashMap<>(stats);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/cancellation-rate")
    @Operation(summary = "% Cancelamento (período)", description = "Percentual de pedidos cancelados no período")
    public ResponseEntity<Map<String, Object>> getCancellationRate(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Map<String, Object> res = Map.of("cancellationRate", dashboardService.getCancellationRate(start, end));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/average-delivery-time")
    @Operation(summary = "Tempo médio de entrega (período)", description = "Média do tempo entre despacho e entrega para pedidos entregues no período (retorna segundos e texto)")
    public ResponseEntity<Map<String, Object>> getAverageDeliveryTime(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);

        OptionalDouble avgSec = dashboardService.getAverageDeliveryTimeInSeconds(start, end);
        Map<String, Object> res;
        if (avgSec.isPresent()) {
            long seconds = Math.round(avgSec.getAsDouble());
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            String text = String.format("%02d:%02d:%02d", hours, minutes, secs);
            res = Map.of("averageSeconds", seconds, "averageText", text);
        } else {
            res = Map.of("averageSeconds", 0, "averageText", "00:00:00");
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/order-peaks")
    @Operation(summary = "Picos de pedidos (período)", description = "Timestamps (createdAt) de pedidos no período para identificar horários de maior movimento")
    public ResponseEntity<List<LocalDateTime>> getOrderPeaks(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getOrderPeaks(start, end));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Top produtos (unidades)", description = "Top N produtos por unidades vendidas no período. Parâmetro 'limit' opcional (default 5)")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        return ResponseEntity.ok(dashboardService.getTopProductsByUnits(start, end, limit));
    }

    @GetMapping("/champion")
    @Operation(summary = "Campeão do período", description = "Produto mais vendido no período; inclui se esteve em promoção")
    public ResponseEntity<Map<String, Object>> getChampion(
            @RequestParam(name = "dta_inicio", required = false) String dtaInicio,
            @RequestParam(name = "dta_fim", required = false) String dtaFim) {
        LocalDateTime start = parseStart(dtaInicio);
        LocalDateTime end = parseEnd(dtaFim);
        Optional<Map<String, Object>> opt = dashboardService.getChampionOfPeriod(start, end);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(Collections.emptyMap()));
    }

}
