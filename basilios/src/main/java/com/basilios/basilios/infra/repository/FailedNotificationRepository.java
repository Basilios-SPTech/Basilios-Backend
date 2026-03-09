package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para notificações que falharam.
 * Usado pelo sistema de retry e pelo painel administrativo.
 */
@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {

    /**
     * Busca todas as notificações não processadas (para reprocessamento)
     */
    List<FailedNotification> findByProcessedFalseOrderByCreatedAtAsc();

    /**
     * Busca notificações não processadas de um pedido específico
     */
    List<FailedNotification> findByOrderIdAndProcessedFalse(Long orderId);

    /**
     * Conta notificações pendentes de processamento
     */
    long countByProcessedFalse();

    /**
     * Busca notificações por email do cliente
     */
    List<FailedNotification> findByClientEmailOrderByCreatedAtDesc(String clientEmail);
}
