package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.app.dto.dashboard.AverageDeliveryTimeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAverageDeliveryTime_withDeliveredOrders() {
        LocalDateTime now = LocalDateTime.now();
        // dispatched 30min atrás, entregue 10min atrás = 20min
        // dispatched 60min atrás, entregue 30min atrás = 30min
        List<Object[]> times = Arrays.asList(
            new Object[]{now.minusMinutes(30), now.minusMinutes(10)},
            new Object[]{now.minusMinutes(60), now.minusMinutes(30)}
        );
        when(orderRepository.findDispatchedAndDeliveredTimesOfDeliveredOrders(any(), any())).thenReturn(times);
        AverageDeliveryTimeDTO dto = dashboardService.getAverageDeliveryTime(now.minusHours(2), now);
        long expectedAvg = ((20 * 60) + (30 * 60)) / 2;
        assertEquals(expectedAvg, dto.getSeconds());
        assertEquals("00:25:00", dto.getText());
    }

    @Test
    void testGetAverageDeliveryTime_noDeliveredOrders() {
        when(orderRepository.findDispatchedAndDeliveredTimesOfDeliveredOrders(any(), any())).thenReturn(Collections.emptyList());
        AverageDeliveryTimeDTO dto = dashboardService.getAverageDeliveryTime(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertEquals(0L, dto.getSeconds());
        assertEquals("00:00:00", dto.getText());
    }

    @Test
    void testGetAverageDeliveryTime_formatting() {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> times = Collections.singletonList(new Object[]{now.minusMinutes(30), now.minusMinutes(10)});
        when(orderRepository.findDispatchedAndDeliveredTimesOfDeliveredOrders(any(), any())).thenReturn(times);
        AverageDeliveryTimeDTO dto = dashboardService.getAverageDeliveryTime(now.minusHours(1), now);
        assertEquals(20 * 60, dto.getSeconds());
        assertEquals("00:20:00", dto.getText());
    }
}
