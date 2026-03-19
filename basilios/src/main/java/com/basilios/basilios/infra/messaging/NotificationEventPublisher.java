package com.basilios.basilios.infra.messaging;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.infra.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publisher de eventos de notificação para o RabbitMQ.
 * Envia eventos para o microserviço email-api processar.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica evento de mudança de status de pedido no RabbitMQ.
     */
    public void publishOrderStatusChanged(Order order, StatusPedidoEnum oldStatus,
                                          StatusPedidoEnum newStatus, String motivo) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventType", "ORDER_STATUS_CHANGED");
            event.put("eventVersion", 1);
            event.put("orderId", order.getId());
            event.put("orderCode", order.getCodigoPedido());
            event.put("oldStatus", oldStatus != null ? oldStatus.name() : null);
            event.put("newStatus", newStatus.name());
            event.put("clientEmail", order.getUsuario().getEmail());
            event.put("clientName", order.getUsuario().getNomeUsuario());
            event.put("motivo", motivo);
            event.put("occurredAt", LocalDateTime.now().toString());
            event.put("source", "basilios-monolith");

            String routingKey = "order.status." + newStatus.name().toLowerCase();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATIONS,
                    routingKey,
                    event
            );

            log.info("Evento publicado no RabbitMQ: pedido={}, {} → {}, routingKey={}",
                    order.getCodigoPedido(), oldStatus, newStatus, routingKey);

        } catch (Exception e) {
            log.error("Erro ao publicar evento no RabbitMQ para pedido {}: {}",
                    order.getCodigoPedido(), e.getMessage());
        }
    }

    /**
     * Publica evento de solicitação de reset de senha no RabbitMQ.
     */
    public void publishPasswordResetRequested(String email, String resetUrl,
                                               String userName, String expiresIn) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventType", "PASSWORD_RESET_REQUESTED");
            event.put("eventVersion", 1);
            event.put("email", email);
            event.put("resetUrl", resetUrl);
            event.put("userName", userName);
            event.put("expiresIn", expiresIn);
            event.put("occurredAt", LocalDateTime.now().toString());
            event.put("source", "basilios-monolith");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATIONS,
                    "auth.password-reset",
                    event
            );

            log.info("Evento de reset de senha publicado no RabbitMQ para: {}", email);

        } catch (Exception e) {
            log.error("Erro ao publicar evento de reset no RabbitMQ para {}: {}",
                    email, e.getMessage());
        }
    }

}
