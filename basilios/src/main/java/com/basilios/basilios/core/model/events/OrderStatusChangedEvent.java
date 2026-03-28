package com.basilios.basilios.core.model.events;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Evento disparado quando o status de um pedido é alterado.
 * Usado pelo sistema de notificações (email, WebSocket, métricas).
 * 
 * Publicado via ApplicationEventPublisher no OrderService.
 * Consumido por listeners com @TransactionalEventListener(AFTER_COMMIT).
 */
@Getter
public class OrderStatusChangedEvent {

    private final Order order;
    private final StatusPedidoEnum oldStatus;
    private final StatusPedidoEnum newStatus;
    private final LocalDateTime timestamp;
    private final String motivo; // Para cancelamentos

    public OrderStatusChangedEvent(Order order, StatusPedidoEnum oldStatus, StatusPedidoEnum newStatus) {
        this(order, oldStatus, newStatus, null);
    }

    public OrderStatusChangedEvent(Order order, StatusPedidoEnum oldStatus, StatusPedidoEnum newStatus, String motivo) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
        this.motivo = motivo;
    }

    /**
     * Verifica se é um evento de criação de pedido
     */
    public boolean isCreation() {
        return oldStatus == null && newStatus == StatusPedidoEnum.PENDENTE;
    }

    /**
     * Verifica se é um evento de cancelamento
     */
    public boolean isCancellation() {
        return newStatus == StatusPedidoEnum.CANCELADO;
    }

    /**
     * Verifica se é um evento de conclusão (entrega)
     */
    public boolean isDelivery() {
        return newStatus == StatusPedidoEnum.ENTREGUE;
    }

    /**
     * Retorna o email do cliente do pedido
     */
    public String getClientEmail() {
        return order.getUsuario().getEmail();
    }

    /**
     * Retorna o nome do cliente do pedido
     */
    public String getClientName() {
        return order.getUsuario().getNomeUsuario();
    }

    @Override
    public String toString() {
        return String.format("OrderStatusChangedEvent[orderId=%d, %s → %s, timestamp=%s]",
                order.getId(), oldStatus, newStatus, timestamp);
    }
}
