package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.dto.order.OrderUpdateDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.TestConfig.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    // JwtUtil mock bean provided via TestConfig to avoid deprecation of @MockBean
    @Autowired
    private com.basilios.basilios.infra.security.JwtUtil jwtUtil;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        public com.basilios.basilios.infra.security.JwtUtil jwtUtil() {
            return Mockito.mock(com.basilios.basilios.infra.security.JwtUtil.class);
        }
    }

    @Test
    void findAll_returnsPage() throws Exception {
        OrderResponseDTO dto = OrderResponseDTO.builder()
                .id(1L)
                .total(BigDecimal.valueOf(10))
                .status(StatusPedidoEnum.PENDENTE)
                .build();

        given(orderService.getAllOrders(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void findById_returnsOrder() throws Exception {
        OrderResponseDTO dto = OrderResponseDTO.builder().id(2L).total(BigDecimal.valueOf(20)).build();
        given(orderService.getOrderById(2L)).willReturn(dto);

        mockMvc.perform(get("/orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void create_returnsCreated() throws Exception {
        OrderRequestDTO req = OrderRequestDTO.builder()
                .addressId(1L)
                .items(List.of())
                .build();

        OrderResponseDTO resp = OrderResponseDTO.builder().id(3L).build();
        given(orderService.createOrder(any(OrderRequestDTO.class))).willReturn(resp);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void update_returnsUpdated() throws Exception {
        OrderUpdateDTO update = OrderUpdateDTO.builder().status(StatusPedidoEnum.CONFIRMADO).build();
        OrderResponseDTO updated = OrderResponseDTO.builder().id(4L).status(StatusPedidoEnum.CONFIRMADO).build();
        given(orderService.updateOrder(eq(4L), any(OrderUpdateDTO.class))).willReturn(updated);

        mockMvc.perform(patch("/orders/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        // softDelete returns void; just ensure endpoint returns 204
        mockMvc.perform(delete("/orders/5"))
                .andExpect(status().isNoContent());
    }
}
