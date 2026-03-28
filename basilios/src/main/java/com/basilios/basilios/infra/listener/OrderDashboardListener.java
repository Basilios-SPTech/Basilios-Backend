package com.basilios.basilios.infra.listener;

import com.basilios.basilios.core.model.events.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener responsável por atualizar o painel de gerenciamento em tempo real via WebSocket.
 * 
 * Envia atualizações para:
 * - /topic/orders: atualizações gerais de pedidos (para todos os clientes conectados)
 * - /topic/orders/{orderId}: atualizações específicas de um pedido
 * - /user/{userId}/queue/orders: notificações privadas para o cliente do pedido
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDashboardListener {

    private final SimpMessagingTemplate messagingTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            Map<String, Object> payload = buildPayload(event);

            // Envia para o tópico geral de pedidos (painel administrativo)
            messagingTemplate.convertAndSend("/topic/orders", payload);
            log.debug("WebSocket: atualização enviada para /topic/orders");

            // Envia para o tópico específico do pedido
            String orderTopic = "/topic/orders/" + event.getOrder().getId();
            messagingTemplate.convertAndSend(orderTopic, payload);
            log.debug("WebSocket: atualização enviada para {}", orderTopic);

            // Envia notificação privada para o cliente do pedido
            Long userId = event.getOrder().getUsuario().getId();
            String userQueue = "/user/" + userId + "/queue/orders";
            messagingTemplate.convertAndSend(userQueue, payload);
            log.debug("WebSocket: notificação enviada para {}", userQueue);

            log.info("Painel atualizado via WebSocket: pedido {} → {}", 
                    event.getOrder().getCodigoPedido(), 
                    event.getNewStatus());

        } catch (Exception e) {
            log.error("Erro ao enviar atualização WebSocket para pedido {}: {}",
                    event.getOrder().getId(), e.getMessage());
            // Não relança exceção - WebSocket é best-effort, não deve bloquear o fluxo
        }
    }

    /**
     * Constrói o payload para envio via WebSocket
     */
    private Map<String, Object> buildPayload(OrderStatusChangedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ORDER_STATUS_CHANGED");
        payload.put("orderId", event.getOrder().getId());
        payload.put("orderCode", event.getOrder().getCodigoPedido());
        payload.put("oldStatus", event.getOldStatus() != null ? event.getOldStatus().name() : null);
        payload.put("newStatus", event.getNewStatus().name());
        payload.put("timestamp", event.getTimestamp().format(FORMATTER));
        payload.put("clientName", event.getClientName());
        
        if (event.isCancellation() && event.getMotivo() != null) {
            payload.put("cancellationReason", event.getMotivo());
        }

        // Dados resumidos do pedido
        payload.put("total", event.getOrder().getTotal());
        
        return payload;
    }
}
