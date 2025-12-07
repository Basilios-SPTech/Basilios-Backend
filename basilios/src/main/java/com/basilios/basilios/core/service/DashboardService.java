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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    private LocalDateTime normalizeStart(LocalDateTime start) {
        return start == null ? LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0) : start;
    }

    private LocalDateTime normalizeEnd(LocalDateTime end) {
        return end == null ? LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999) : end;
    }

    public RevenueDTO getRevenue(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        BigDecimal revenue = orders.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return RevenueDTO.toResponse(revenue);
    }

    public OrdersCountDTO getOrdersCount(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        long count = orderRepository.countByCreatedAtBetween(start, end);
        return OrdersCountDTO.toResponse(count);
    }

    public AverageDeliveryTimeDTO getAverageDeliveryTime(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Object[]> times = orderRepository.findDispatchedAndDeliveredTimesOfDeliveredOrders(start, end);
        if (times.isEmpty()) {
            return AverageDeliveryTimeDTO.toResponse(0L, "00:00:00");
        }
        long totalSeconds = 0L;
        for (Object[] pair : times) {
            LocalDateTime dispatched = (LocalDateTime) pair[0];
            LocalDateTime delivered = (LocalDateTime) pair[1];
            totalSeconds += java.time.Duration.between(dispatched, delivered).getSeconds();
        }
        long avgSeconds = totalSeconds / times.size();
        long hours = avgSeconds / 3600;
        long minutes = (avgSeconds % 3600) / 60;
        long secs = avgSeconds % 60;
        String text = String.format("%02d:%02d:%02d", hours, minutes, secs);
        return AverageDeliveryTimeDTO.toResponse(avgSeconds, text);
    }

    public long getItemsSold(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        Long qty = productOrderRepository.sumQuantityByDeliveredOrdersInPeriod(start, end);
        return qty == null ? 0L : qty;
    }

    public long getCancelledOrdersCount(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        return orderRepository.countByStatusAndCreatedAtBetween(StatusPedidoEnum.CANCELADO, start, end);
    }

    public OrderPeaksDTO getOrderPeaks(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<LocalDateTime> peaks = orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(start, end).stream()
                .map(Order::getCreatedAt)
                .collect(Collectors.toList());
        return OrderPeaksDTO.builder().peaks(peaks).build();
    }

    public List<TopProductDTO> getTopProductsByUnits(LocalDateTime start, LocalDateTime end, int limit) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Object[]> rows = productOrderRepository.findBestSellingProductsByPeriod(start, end);
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

    public Optional<ChampionDTO> getChampionOfPeriod(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Object[]> rows = productOrderRepository.findBestSellingProductsByPeriod(start, end);
        if (rows == null || rows.isEmpty()) return Optional.empty();
        Object[] top = rows.get(0);
        Product p = (Product) top[0];
        Number units = (Number) top[1];
        boolean onPromo = productOrderRepository.existsPromotionForProductInPeriod(p.getId(), start, end);
        return Optional.of(new ChampionDTO(
            p.getId(),
            p.getName(),
            units != null ? units.intValue() : 0,
            onPromo
        ));
    }

    public AverageTicketDTO getAverageTicket(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        if (orders.isEmpty()) {
            return AverageTicketDTO.toResponse(BigDecimal.ZERO);
        }
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = orders.size();
        BigDecimal averageTicket = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return AverageTicketDTO.toResponse(averageTicket);
    }

    public ItemsSoldDTO getItemsSoldData(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        Long totalItemsSold = productOrderRepository.sumQuantityByDeliveredOrdersInPeriod(start, end);
        return ItemsSoldDTO.toResponse(totalItemsSold != null ? totalItemsSold : 0L);
    }

    public CancellationRateDTO getCancellationRate(LocalDateTime start, LocalDateTime end) {
        start = normalizeStart(start);
        end = normalizeEnd(end);
        long totalOrders = orderRepository.countByCreatedAtBetween(start, end);
        long cancelledOrders = orderRepository.countByStatusAndCreatedAtBetween(StatusPedidoEnum.CANCELADO, start, end);
        double cancellationRate = totalOrders > 0 ? ((double) cancelledOrders / totalOrders) * 100.0 : 0.0;
        return CancellationRateDTO.toResponse(cancellationRate);
    }
}
