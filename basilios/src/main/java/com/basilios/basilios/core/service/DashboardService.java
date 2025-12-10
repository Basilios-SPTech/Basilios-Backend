package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.dashboard.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    private LocalDateTime getStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime getEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }

    public RevenueDTO getRevenue(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        BigDecimal revenue = orderRepository.sumTotalByCreatedAtBetweenEntregue(startDt, endDt);
        return RevenueDTO.toResponse(revenue);
    }

    public OrdersCountDTO getOrdersCount(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        long count = orderRepository.countByCreatedAtBetween(startDt, endDt);
        return OrdersCountDTO.builder().orders(count).build();
    }

    public AverageDeliveryTimeDTO getAverageDeliveryTime(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        // Buscar apenas pedidos ENTREGUES no período
        List<Order> deliveredOrders = orderRepository.findByStatusAndCreatedAtBetween(StatusPedidoEnum.ENTREGUE, startDt, endDt);
        if (deliveredOrders.isEmpty()) {
            return AverageDeliveryTimeDTO.toResponse(0L, "00:00:00");
        }
        long totalSeconds = 0L;
        int count = 0;
        for (Order order : deliveredOrders) {
            LocalDateTime dispatched = order.getDispatchedAt();
            LocalDateTime delivered = order.getDeliveredAt();
            if (dispatched != null && delivered != null && delivered.isAfter(dispatched)) {
                totalSeconds += java.time.Duration.between(dispatched, delivered).getSeconds();
                count++;
            }
        }
        if (count == 0) {
            return AverageDeliveryTimeDTO.toResponse(0L, "00:00:00");
        }
        long avgSeconds = totalSeconds / count;
        long hours = avgSeconds / 3600;
        long minutes = (avgSeconds % 3600) / 60;
        long secs = avgSeconds % 60;
        String text = String.format("%02d:%02d:%02d", hours, minutes, secs);
        return AverageDeliveryTimeDTO.toResponse(avgSeconds, text);
    }

    private LocalDateTime normalizeStart(LocalDateTime start) {
        if (start != null) return start;
        return LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime normalizeEnd(LocalDateTime end) {
        if (end != null) return end;
        return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }

    public long getItemsSold(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        Long qty = productOrderRepository.sumQuantityByDeliveredOrdersInPeriod(start, end);
        return qty == null ? 0L : qty;
    }

    public long getCancelledOrdersCount(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        return orderRepository.countByStatusAndCreatedAtBetween(StatusPedidoEnum.CANCELADO, startDt, endDt);
    }

    public OrderPeaksDTO getOrderPeaks(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        List<LocalDateTime> peaks = orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(startDt, endDt).stream()
                .map(Order::getCreatedAt)
                .collect(Collectors.toList());
        return OrderPeaksDTO.builder().peaks(peaks).build();
    }

    public List<TopProductDTO> getTopProductsByUnits(LocalDate start, LocalDate end, int limit) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        List<Object[]> rows = productOrderRepository.findBestSellingProductsByPeriod(startDt, endDt);
        List<TopProductDTO> dtos = new ArrayList<>();
        for (Object[] row : rows.stream().limit(limit).toList()) {
            Product p = (Product) row[0];
            Number totalSold = (Number) row[1];
            dtos.add(new TopProductDTO(
                p.getId(),
                p.getName(),
                totalSold != null ? totalSold.intValue() : 0
            ));
        }
        return dtos;
    }

    public Optional<ChampionDTO> getChampionOfPeriod(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        List<Object[]> rows = productOrderRepository.findBestSellingProductsByPeriod(startDt, endDt);
        if (rows == null || rows.isEmpty()) return Optional.empty();
        Object[] top = rows.get(0);
        Product p = (Product) top[0];
        Number units = (Number) top[1];
        boolean onPromo = productOrderRepository.existsPromotionForProductInPeriod(p.getId(), startDt, endDt);
        return Optional.of(new ChampionDTO(
            p.getId(),
            p.getName(),
            units != null ? units.intValue() : 0,
            onPromo
        ));
    }

    public AverageTicketDTO getAverageTicket(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        BigDecimal totalRevenue = getRevenue(start, end).getRevenue();
        long totalOrders = orderRepository.countByStatusAndCreatedAtBetween(StatusPedidoEnum.ENTREGUE, startDt, endDt);
        BigDecimal averageTicket = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return AverageTicketDTO.toResponse(averageTicket);
    }


    public CancellationRateDTO getCancellationRate(LocalDate start, LocalDate end) {
        LocalDateTime startDt = getStartOfDay(start);
        LocalDateTime endDt = getEndOfDay(end);
        long totalOrders = orderRepository.countByCreatedAtBetween(startDt, endDt);
        long cancelledOrders = orderRepository.countCancelledOrdersByCreatedAtBetween(startDt, endDt);
        double cancellationRate = totalOrders > 0 ? ((double) cancelledOrders / totalOrders) * 100.0 : 0.0;
        return CancellationRateDTO.toResponse(cancellationRate);
    }
}
