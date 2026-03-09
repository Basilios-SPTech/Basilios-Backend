package com.basilios.basilios.infra.listener;

import com.basilios.basilios.core.model.FailedNotification;
import com.basilios.basilios.core.model.events.OrderStatusChangedEvent;
import com.basilios.basilios.core.service.EmailService;
import com.basilios.basilios.infra.repository.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener responsável por enviar notificações por email quando o status de um pedido muda.
 * 
 * Características:
 * - @TransactionalEventListener(AFTER_COMMIT): só executa após commit da transação
 * - @Async: executa em thread separada, não bloqueia o fluxo principal
 * - @Retryable: tenta 3 vezes com backoff exponencial (1s, 2s, 4s)
 * - @Recover: após 3 falhas, salva na tabela failed_notifications para reprocessamento
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final EmailService emailService;
    private final FailedNotificationRepository failedNotificationRepository;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Processando notificação para pedido {}: {} → {}", 
                event.getOrder().getCodigoPedido(),
                event.getOldStatus(),
                event.getNewStatus());

        String email = event.getClientEmail();
        String clientName = event.getClientName();
        String orderCode = event.getOrder().getCodigoPedido();

        switch (event.getNewStatus()) {
            case CONFIRMADO -> emailService.sendOrderConfirmedEmail(email, clientName, orderCode);
            case PREPARANDO -> emailService.sendOrderPreparingEmail(email, clientName, orderCode);
            case DESPACHADO -> emailService.sendOrderDispatchedEmail(email, clientName, orderCode);
            case ENTREGUE -> emailService.sendOrderDeliveredEmail(email, clientName, orderCode);
            case CANCELADO -> emailService.sendOrderCancelledEmail(email, clientName, orderCode, event.getMotivo());
            default -> log.debug("Status {} não requer notificação por email", event.getNewStatus());
        }

        log.info("Notificação enviada com sucesso para {} (pedido {})", email, orderCode);
    }

    /**
     * Fallback após 3 tentativas falhas.
     * Salva a notificação na tabela failed_notifications para reprocessamento posterior.
     */
    @Recover
    public void recoverFailedNotification(Exception ex, OrderStatusChangedEvent event) {
        log.error("Falha definitiva ao enviar notificação para pedido {} após 3 tentativas: {}",
                event.getOrder().getCodigoPedido(), ex.getMessage());

        try {
            FailedNotification failedNotification = FailedNotification.builder()
                    .orderId(event.getOrder().getId())
                    .orderCode(event.getOrder().getCodigoPedido())
                    .clientEmail(event.getClientEmail())
                    .clientName(event.getClientName())
                    .oldStatus(event.getOldStatus() != null ? event.getOldStatus().name() : null)
                    .newStatus(event.getNewStatus().name())
                    .motivo(event.getMotivo())
                    .errorMessage(ex.getMessage())
                    .attemptCount(3)
                    .build();

            failedNotificationRepository.save(failedNotification);
            log.info("Notificação falha salva para reprocessamento: orderId={}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("Erro ao salvar notificação falha no banco: {}", e.getMessage());
        }
    }
}
