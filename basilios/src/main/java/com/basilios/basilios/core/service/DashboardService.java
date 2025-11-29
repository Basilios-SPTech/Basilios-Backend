package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.dashboard.*;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductOrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    // ========== Helpers ==========

    private LocalDateTime normalizeStart(LocalDateTime start) {
        return start == null ? LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0) : start;
    }

    private LocalDateTime normalizeEnd(LocalDateTime end) {
        return end == null ? LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999) : end;
    }

    // ========== KPIs solicitados ==========

    /**
         * RECEITA (ITENS): Soma total do valor (dinheiro) de todos os pedidos realizados no período.
     */
    public BigDecimal getRevenue(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        return orders.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * PEDIDOS: Quantidade total de pedidos realizados no período.
     */
    public long getOrdersCount(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        return orderRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * TICKET MÉDIO: Valor da Receita total dividido pela quantidade de Pedidos.
     */
    public BigDecimal getAverageTicket(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);

        long orders = getOrdersCount(start, end);
        if (orders == 0) return BigDecimal.ZERO;

        BigDecimal revenue = getRevenue(start, end);
        return revenue.divide(new BigDecimal(orders), 2, RoundingMode.HALF_UP);
    }

    /**
     * ITENS VENDIDOS: Quantidade total de itens vendidos dentro do período (apenas pedidos entregues).
     */
    public long getItemsSold(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        Long qty = productOrderRepository.sumQuantityByDeliveredOrdersInPeriod(start, end);
        return qty == null ? 0L : qty;
    }

    /**
     * % CANCELAMENTO: (Total de Pedidos - Pedidos Cancelados) / Total de Pedidos * 100
     */
    public double getCancellationRate(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);

        long total = getOrdersCount(start, end);
        if (total == 0) return 0.0;

        long cancelled = orderRepository.countByStatusAndCreatedAtBetween(StatusPedidoEnum.CANCELADO, start, end);
        double rate = ((double) (total - cancelled) / (double) total) * 100.0;
        return rate;
    }

    /**
     * TEMPO MÉDIO DE ENTREGA: considerar apenas pedidos entregues; media(delivered_at - dispatched_at)
     * Filtragem por período considera deliveredAt no intervalo para refletir entregas ocorridas no período.
     */
    public OptionalDouble getAverageDeliveryTimeInSeconds(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Order> orders = orderRepository.findByDeliveredAtBetween(start, end);
        List<Long> durations = orders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDispatchedAt() != null)
                .map(o -> java.time.Duration.between(o.getDispatchedAt(), o.getDeliveredAt()).getSeconds())
                .toList();
        return durations.stream().mapToLong(Long::longValue).average();
    }

    /**
     * PICOS DE PEDIDOS: retorna lista de timestamps (createdAt) de pedidos no período
     */
    public List<LocalDateTime> getOrderPeaks(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        return orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(start, end).stream()
                .map(Order::getCreatedAt)
                .toList();
    }

    /**
     * TOP N PRODUTOS (UNIDADES) no período
     */
    public List<Map<String, Object>> getTopProductsByUnits(LocalDateTime start, LocalDateTime end, int limit) {
        start = normalizeStart(start);
        end = normalizeEnd(end);

        return productOrderRepository.findBestSellingProductsByPeriod(start, end).stream()
                .limit(limit)
                .map(row -> {
                    Product p = (Product) row[0];
                    Number totalSold = (Number) row[1];
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put("unitsSold", totalSold != null ? totalSold.longValue() : 0L);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * CAMPEÃO DO PERÍODO: produto mais vendido (unidades) e se estava em promoção
     */
    public Optional<Map<String, Object>> getChampionOfPeriod(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);

        List<Object[]> rows = productOrderRepository.findBestSellingProductsByPeriod(start, end);
        if (rows == null || rows.isEmpty()) return Optional.empty();

        Object[] top = rows.get(0);
        Product p = (Product) top[0];
        Number units = (Number) top[1];
        boolean onPromo = productOrderRepository.existsPromotionForProductInPeriod(p.getId(), start, end);

        Map<String, Object> res = new HashMap<>();
        res.put("id", p.getId());
        res.put("name", p.getName());
        res.put("unitsSold", units != null ? units.longValue() : 0L);
        res.put("onPromotion", onPromo);
        return Optional.of(res);
    }

    public List<TopProductDTO> getTopProductsByUnitsDTO(LocalDateTime start, LocalDateTime end, int limit) {
        List<Map<String, Object>> raw = getTopProductsByUnits(start, end, limit);
        List<TopProductDTO> dtos = new ArrayList<>();
        for (Map<String, Object> map : raw) {
            dtos.add(new TopProductDTO(
                ((Number) map.get("id")).longValue(),
                (String) map.get("name"),
                ((Number) map.get("unitsSold")).intValue()
            ));
        }
        return dtos;
    }

    public Optional<ChampionDTO> getChampionOfPeriodDTO(LocalDateTime start, LocalDateTime end) {
        Optional<Map<String, Object>> opt = getChampionOfPeriod(start, end);
        if (opt.isEmpty()) return Optional.empty();
        Map<String, Object> map = opt.get();
        return Optional.of(new ChampionDTO(
            ((Number) map.get("id")).longValue(),
            (String) map.get("name"),
            ((Number) map.get("unitsSold")).intValue(),
            (Boolean) map.getOrDefault("onPromotion", false)
        ));
    }

    public RevenueDTO getRevenueDTO(LocalDateTime start, LocalDateTime end) {
        return RevenueDTO.toResponse(getRevenue(start, end));
    }

    public OrdersCountDTO getOrdersCountDTO(LocalDateTime start, LocalDateTime end) {
        return OrdersCountDTO.toResponse(getOrdersCount(start, end));
    }

    public AverageTicketDTO getAverageTicketDTO(LocalDateTime start, LocalDateTime end) {
        return AverageTicketDTO.toResponse(getAverageTicket(start, end));
    }

    public ItemsSoldDTO getItemsSoldDTO(LocalDateTime start, LocalDateTime end) {
        return ItemsSoldDTO.toResponse(getItemsSold(start, end));
    }

    public CancellationRateDTO getCancellationRateDTO(LocalDateTime start, LocalDateTime end) {
        return CancellationRateDTO.toResponse(getCancellationRate(start, end));
    }

    public AverageDeliveryTimeDTO getAverageDeliveryTimeDTO(LocalDateTime start, LocalDateTime end) {
        OptionalDouble avgSec = getAverageDeliveryTimeInSeconds(start, end);
        if (avgSec.isPresent()) {
            long seconds = Math.round(avgSec.getAsDouble());
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            String text = String.format("%02d:%02d:%02d", hours, minutes, secs);
            return AverageDeliveryTimeDTO.toResponse(seconds, text);
        } else {
            return AverageDeliveryTimeDTO.toResponse(0L, "00:00:00");
        }
    }

    public OrderPeaksDTO getOrderPeaksDTO(LocalDateTime start, LocalDateTime end) {
        return OrderPeaksDTO.builder().peaks(getOrderPeaks(start, end)).build();
    }

    public long getCancelledOrdersCount(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        return orderRepository.countByStatusAndCreatedAtBetween(com.basilios.basilios.core.enums.StatusPedidoEnum.CANCELADO, start, end);
    }
}
