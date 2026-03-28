package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.dashboard.AverageDeliveryTimeDTO;
import com.basilios.basilios.app.dto.dashboard.RevenueDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do DashboardService")
class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);
        startDateTime = startDate.atStartOfDay();
        endDateTime = endDate.atTime(23, 59, 59, 999_999_999);
    }

    // ========== TESTES DO MÉTODO getRevenue() ==========

    @Test
    @DisplayName("Deve retornar receita total do período quando há pedidos entregues")
    void getRevenue_DeveRetornarReceitaTotalDoPeriodo() {
        // Arrange
        BigDecimal receitaEsperada = new BigDecimal("1500.00");
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(receitaEsperada);

        // Act
        RevenueDTO result = dashboardService.getRevenue(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(receitaEsperada, result.getRevenue());
        verify(orderRepository, times(1)).sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve retornar BigDecimal.ZERO quando não há pedidos no período")
    void getRevenue_DeveRetornarZeroQuandoNaoHaPedidos() {
        // Arrange
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        // Act
        RevenueDTO result = dashboardService.getRevenue(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getRevenue());
        verify(orderRepository, times(1)).sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve calcular receita corretamente com valores decimais")
    void getRevenue_DeveCalcularReceitaComValoresDecimais() {
        // Arrange
        BigDecimal receitaEsperada = new BigDecimal("2547.89");
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(receitaEsperada);

        // Act
        RevenueDTO result = dashboardService.getRevenue(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(0, receitaEsperada.compareTo(result.getRevenue()));
    }

    @Test
    @DisplayName("Deve considerar apenas pedidos entregues no cálculo da receita")
    void getRevenue_DeveConsiderarApenasPedidosEntregues() {
        // Arrange
        BigDecimal receita = new BigDecimal("3000.00");
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(receita);

        // Act
        dashboardService.getRevenue(startDate, endDate);

        // Assert
        // Verifica que está chamando o método correto que filtra apenas ENTREGUES
        verify(orderRepository, times(1)).sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderRepository, never()).findByCreatedAtBetweenOrderByCreatedAtAsc(any(), any());
    }

    @Test
    @DisplayName("Deve usar período padrão quando datas são null")
    void getRevenue_DeveUsarPeriodoPadraoQuandoDatasNulas() {
        // Arrange
        BigDecimal receita = new BigDecimal("500.00");
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(receita);

        // Act
        RevenueDTO result = dashboardService.getRevenue(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(receita, result.getRevenue());
        verify(orderRepository, times(1)).sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve retornar receita alta quando há muitos pedidos")
    void getRevenue_DeveRetornarReceitaAltaComMuitosPedidos() {
        // Arrange
        BigDecimal receitaAlta = new BigDecimal("50000.00");
        when(orderRepository.sumTotalByCreatedAtBetweenEntregue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(receitaAlta);

        // Act
        RevenueDTO result = dashboardService.getRevenue(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRevenue().compareTo(new BigDecimal("10000")) > 0);
    }

    // ========== TESTES DO MÉTODO getAverageDeliveryTime() ==========

    @Test
    @DisplayName("Deve retornar tempo médio de entrega quando há pedidos entregues")
    void getAverageDeliveryTime_DeveRetornarTempoMedioComPedidosEntregues() {
        // Arrange
        Order order1 = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 19, 0),   // dispatched
                LocalDateTime.of(2024, 1, 15, 19, 30)   // delivered (30 min)
        );
        Order order2 = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 20, 0),   // dispatched
                LocalDateTime.of(2024, 1, 15, 20, 50)   // delivered (50 min)
        );
        // Média: (30 + 50) / 2 = 40 minutos = 2400 segundos

        List<Order> orders = List.of(order1, order2);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2400L, result.getAverageSeconds());
    }

    @Test
    @DisplayName("Deve retornar zero quando não há pedidos entregues")
    void getAverageDeliveryTime_DeveRetornarZeroQuandoNaoHaPedidos() {
        // Arrange
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getAverageSeconds());
    }

    @Test
    @DisplayName("Deve ignorar pedidos sem data de despacho ou entrega")
    void getAverageDeliveryTime_DeveIgnorarPedidosSemDatas() {
        // Arrange
        Order orderValido = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 19, 0),
                LocalDateTime.of(2024, 1, 15, 19, 30)
        );

        Order orderSemDispatch = new Order();
        orderSemDispatch.setDispatchedAt(null);
        orderSemDispatch.setDeliveredAt(LocalDateTime.of(2024, 1, 15, 20, 0));

        Order orderSemDelivery = new Order();
        orderSemDelivery.setDispatchedAt(LocalDateTime.of(2024, 1, 15, 20, 0));
        orderSemDelivery.setDeliveredAt(null);

        List<Order> orders = List.of(orderValido, orderSemDispatch, orderSemDelivery);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1800L, result.getAverageSeconds()); // 30 minutos do pedido válido
    }

    @Test
    @DisplayName("Deve calcular corretamente tempo de entrega em horas")
    void getAverageDeliveryTime_DeveCalcularTempoEmHoras() {
        // Arrange
        Order order = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 18, 0),
                LocalDateTime.of(2024, 1, 15, 20, 30)  // 2h30min = 9000 segundos
        );

        List<Order> orders = List.of(order);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(9000L, result.getAverageSeconds());
    }

    @Test
    @DisplayName("Deve formatar corretamente tempo com zeros à esquerda")
    void getAverageDeliveryTime_DeveFormatarComZerosAEsquerda() {
        // Arrange
        Order order = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 19, 0),
                LocalDateTime.of(2024, 1, 15, 19, 5)  // 5 minutos
        );

        List<Order> orders = List.of(order);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(300L, result.getAverageSeconds());
    }

    @Test
    @DisplayName("Deve considerar apenas pedidos com status ENTREGUE")
    void getAverageDeliveryTime_DeveConsiderarApenasPedidosEntregues() {
        // Arrange
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        verify(orderRepository, times(1)).findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        verify(orderRepository, never()).findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.PENDENTE),
                any(),
                any());
    }

    @Test
    @DisplayName("Deve calcular média correta com múltiplos pedidos")
    void getAverageDeliveryTime_DeveCalcularMediaComMultiplosPedidos() {
        // Arrange
        Order order1 = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 19, 0),
                LocalDateTime.of(2024, 1, 15, 19, 20)  // 20 min
        );
        Order order2 = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 20, 0),
                LocalDateTime.of(2024, 1, 15, 20, 40)  // 40 min
        );
        Order order3 = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 21, 0),
                LocalDateTime.of(2024, 1, 15, 21, 30)  // 30 min
        );
        // Média: (20 + 40 + 30) / 3 = 30 min

        List<Order> orders = List.of(order1, order2, order3);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1800L, result.getAverageSeconds());
        assertEquals(1800, result.getAverageSeconds());
    }

    @Test
    @DisplayName("Deve ignorar pedidos com delivery anterior ao dispatch")
    void getAverageDeliveryTime_DeveIgnorarPedidosComDatasInvalidas() {
        // Arrange
        Order orderValido = createDeliveredOrder(
                LocalDateTime.of(2024, 1, 15, 19, 0),
                LocalDateTime.of(2024, 1, 15, 19, 30)
        );

        Order orderInvalido = new Order();
        orderInvalido.setDispatchedAt(LocalDateTime.of(2024, 1, 15, 20, 0));
        orderInvalido.setDeliveredAt(LocalDateTime.of(2024, 1, 15, 19, 0)); // Antes do dispatch!

        List<Order> orders = List.of(orderValido, orderInvalido);
        when(orderRepository.findByStatusAndCreatedAtBetween(
                eq(StatusPedidoEnum.ENTREGUE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        AverageDeliveryTimeDTO result = dashboardService.getAverageDeliveryTime(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1800L, result.getAverageSeconds()); // Apenas o pedido válido
    }

    private Order createDeliveredOrder(LocalDateTime dispatchedAt, LocalDateTime deliveredAt) {
        Order order = new Order();
        order.setStatus(StatusPedidoEnum.ENTREGUE);
        order.setDispatchedAt(dispatchedAt);
        order.setDeliveredAt(deliveredAt);
        return order;
    }
}