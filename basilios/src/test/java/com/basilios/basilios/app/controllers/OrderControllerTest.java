package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.CancelOrderDTO;
import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.dto.order.UpdateOrderStatusDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OrderService orderService;

    private OrderResponseDTO orderResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        orderService = mock(OrderService.class);

        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        orderResponse = new OrderResponseDTO();
        orderResponse.setId(1L);
        orderResponse.setStatus(StatusPedidoEnum.PENDENTE);
    }

    // ========== ENDPOINTS DE CLIENTE ==========

    @Test
    @DisplayName("POST /orders - Deve criar pedido com sucesso")
    void createOrder_DeveCriarPedidoComSucesso() throws Exception {
        OrderRequestDTO.OrderItemRequest item = new OrderRequestDTO.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setAddressId(1L);
        request.setItems(List.of(item));

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));

        verify(orderService).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("GET /orders/me - Deve listar pedidos do cliente")
    void getMyOrders_DeveListarPedidosDoCliente() throws Exception {
        when(orderService.getUserOrders()).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(orderService).getUserOrders();
    }

    @Test
    @DisplayName("GET /orders/me/{id} - Deve retornar pedido do cliente por ID")
    void getMyOrderById_DeveRetornarPedidoDoCliente() throws Exception {
        when(orderService.getUserOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/me/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(orderService).getUserOrderById(1L);
    }

    @Test
    @DisplayName("PATCH /orders/me/{id}/cancel - Deve cancelar pedido do cliente")
    void cancelMyOrder_DeveCancelarPedidoDoCliente() throws Exception {
        CancelOrderDTO cancelDTO = new CancelOrderDTO();
        cancelDTO.setMotivo("Desisti");

        when(orderService.cancelarPedidoUsuario(eq(1L), eq("Desisti"))).thenReturn(orderResponse);

        mockMvc.perform(patch("/orders/me/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(orderService).cancelarPedidoUsuario(1L, "Desisti");
    }

    // ========== ENDPOINTS DE STAFF/ADMIN ==========

    @Test
    @DisplayName("GET /orders/{id} - Deve retornar pedido por ID (staff)")
    void findById_DeveRetornarPedidoPorId() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(orderService).getOrderById(1L);
    }

    @Test
    @DisplayName("GET /orders/by-status - Deve retornar pedidos por status")
    void getOrdersByStatus_DeveRetornarPedidosPorStatus() throws Exception {
        when(orderService.getOrdersByStatus(StatusPedidoEnum.PENDENTE))
                .thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/orders/by-status")
                        .param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(orderService).getOrdersByStatus(StatusPedidoEnum.PENDENTE);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status - Deve atualizar status do pedido")
    void updateOrderStatus_DeveAtualizarStatus() throws Exception {
        UpdateOrderStatusDTO dto = new UpdateOrderStatusDTO();
        dto.setStatus("CONFIRMADO");

        when(orderService.updateOrderStatus(eq(1L), eq("CONFIRMADO"))).thenReturn(orderResponse);

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(orderService).updateOrderStatus(1L, "CONFIRMADO");
    }
}
