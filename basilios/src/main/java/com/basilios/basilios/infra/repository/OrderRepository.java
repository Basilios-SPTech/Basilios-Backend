package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Busca pedidos de um usuário ordenados por data (mais recentes primeiro)
     * Pré-carrega productOrders e product para evitar N+1
     */
    @EntityGraph(attributePaths = {"productOrders", "productOrders.product"})
    List<Order> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    /**
     * Busca pedidos de um usuário
     */
    List<Order> findByUsuario(Usuario usuario);

    /**
     * Busca pedidos por status
     */
    List<Order> findByStatus(StatusPedidoEnum status);

    /**
     * Busca pedidos pendentes (PENDENTE)
     */
    @Query("SELECT o FROM Order o WHERE o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.PENDENTE ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders();

    /**
     * Busca pedidos em andamento (CONFIRMADO, PREPARANDO, DESPACHADO)
     */
    @Query("SELECT o FROM Order o WHERE o.status IN (com.basilios.basilios.core.enums.StatusPedidoEnum.CONFIRMADO, com.basilios.basilios.core.enums.StatusPedidoEnum.PREPARANDO, com.basilios.basilios.core.enums.StatusPedidoEnum.DESPACHADO) ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders();

    /**
     * Busca pedidos de um usuário por status
     */
    List<Order> findByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    /**
     * Busca pedidos criados após uma data
     */
    List<Order> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Busca pedidos de um usuário criados após uma data
     */
    List<Order> findByUsuarioAndCreatedAtAfter(Usuario usuario, LocalDateTime date);

    /**
     * Conta total de pedidos de um usuário
     */
    long countByUsuario(Usuario usuario);

    /**
     * Conta pedidos de um usuário por status
     */
    long countByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    /**
     * Busca últimos N pedidos de um usuário
     */
    // (method defined above) - single definition kept

    /**
     * Busca pedidos por status ordenados por data asc
     */
    List<Order> findByStatusOrderByCreatedAtAsc(StatusPedidoEnum status);

    /**
     * Busca pedidos por múltiplos status (ex: confirmado, preparando, despachado)
     */
    List<Order> findByStatusInOrderByCreatedAtAsc(List<StatusPedidoEnum> statuses);

    // ========== NOVOS MÉTODOS PARA DASHBOARD E ANÁLISES POR PERÍODO ==========

    /**
     * Busca pedidos criados entre duas datas (inclusivo)
     */
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Conta pedidos criados entre duas datas
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Conta pedidos com um determinado status dentro do período
     */
    long countByStatusAndCreatedAtBetween(StatusPedidoEnum status, LocalDateTime start, LocalDateTime end);

    /**
     * Busca pedidos do período ordenados por createdAt (útil para identificar picos)
     */
    List<Order> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime start, LocalDateTime end);

    /**
     * Busca pedidos entregues entre duas datas (deliveredAt)
     */
    List<Order> findByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);
}