package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductOrderRepository productOrderRepository;

    private final OrderRepository orderRepository;

    private final ProductOrderService productOrderService;

    @Autowired
    public DashboardService(ProductOrderRepository productOrderRepository, OrderRepository orderRepository, ProductOrderService productOrderService) {
        this.productOrderRepository = productOrderRepository;
        this.orderRepository = orderRepository;
        this.productOrderService = productOrderService;
    }

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

        // Use repository aggregation to avoid loading all Order entities into memory
        BigDecimal total = orderRepository.sumTotalByCreatedAtBetweenExcludeCancelled(start, end);
        if (total == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
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

        // Use only non-cancelled orders for the ticket calculation
        long nonCancelledOrders = orderRepository.countByStatusNotAndCreatedAtBetween(StatusPedidoEnum.CANCELADO, start, end);
        if (nonCancelledOrders == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal revenue = getRevenue(start, end);
        return revenue.divide(new BigDecimal(nonCancelledOrders), 2, RoundingMode.HALF_UP);
    }

    /**
     * ITENS VENDIDOS: Quantidade total de itens vendidos dentro do período.
     */
    public long getItemsSold(LocalDateTime start, LocalDateTime end) {
        // delegate to ProductOrderService; we can pass nulls (service/repo handles defaults) but normalize for consistency
        LocalDateTime nStart = normalizeStart(start);
        LocalDateTime nEnd = normalizeEnd(end);
        return productOrderService.getItemsSold(nStart, nEnd);
    }

    /**
     * ITENS VENDIDOS E NÃO VENDIDOS: Retorna um mapa com quantidade de itens vendidos e quantidade de produtos
     * que não tiveram vendas no período.
     */
    public Map<String, Long> getItemsSoldAndNotSold(LocalDateTime start, LocalDateTime end) {
        LocalDateTime nStart = normalizeStart(start);
        LocalDateTime nEnd = normalizeEnd(end);

        long itemsSold = productOrderService.getItemsSold(nStart, nEnd);
        long productsNotSold = productOrderService.getProductsNotSold(nStart, nEnd);

        Map<String, Long> result = new HashMap<>();
        result.put("itemsSold", itemsSold);
        result.put("productsNotSold", productsNotSold);
        return result;
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
        return ((double) (total - cancelled) / (double) total) * 100.0;
    }

    /**
     * TEMPO MÉDIO DE ENTREGA: considerar apenas pedidos entregues; media(delivered_at - dispatched_at)
     * Filtragem por período considera deliveredAt no intervalo para refletir entregas ocorridas no período.
     */
    public OptionalDouble getAverageDeliveryTimeInSeconds(LocalDateTime start, LocalDateTime end) {
        // We'll fetch orders by deliveredAt between start/end.
        start = normalizeStart(start);
        end = normalizeEnd(end);

        // Prefer orders delivered in the period (deliveredAt between start/end)
        List<Order> orders = orderRepository.findByDeliveredAtBetween(start, end);

        // Prefer orders with deliveredAt not null and dispatchedAt not null
        List<Long> durations = orders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDispatchedAt() != null)
                .map(o -> Duration.between(o.getDispatchedAt(), o.getDeliveredAt()).getSeconds())
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
                .toList();
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

}
