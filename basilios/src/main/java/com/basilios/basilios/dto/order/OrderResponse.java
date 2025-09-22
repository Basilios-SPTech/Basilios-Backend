package com.basilios.basilios.dto.order;

import com.basilios.basilios.enums.StatusPedidoEnum;
import com.basilios.basilios.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<Order.OrderItem> items;
    private BigDecimal total;
    private StatusPedidoEnum status;
    private LocalDateTime createdAt;
    private Boolean redirectToPartners;
    private Map<String, String> partnerLinks;
}