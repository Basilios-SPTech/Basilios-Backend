package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ===== BUSCAS =====
    /**
     * Busca pedidos de um usuário ordenados por data (mais recentes primeiro)
     * Pré-carrega productOrders e product para evitar N+1
     */
    @EntityGraph(attributePaths = {"productOrders", "productOrders.product"})
    List<Order> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    /** Busca pedidos de um usuário */
    List<Order> findByUsuario(Usuario usuario);

    /** Busca pedidos por status */
    List<Order> findByStatus(StatusPedidoEnum status);

    /** Busca pedidos pendentes (PENDENTE) */
    @Query("SELECT o FROM Order o WHERE o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.PENDENTE ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders();

    /** Busca pedidos em andamento (CONFIRMADO, PREPARANDO, DESPACHADO) */
    @Query("SELECT o FROM Order o WHERE o.status IN (com.basilios.basilios.core.enums.StatusPedidoEnum.CONFIRMADO, com.basilios.basilios.core.enums.StatusPedidoEnum.PREPARANDO, com.basilios.basilios.core.enums.StatusPedidoEnum.DESPACHADO) ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders();

    /** Busca pedidos de um usuário por status */
    List<Order> findByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    /** Busca pedidos por status ordenados por data asc */
    List<Order> findByStatusOrderByCreatedAtAsc(StatusPedidoEnum status);

    /** Busca pedidos por múltiplos status ordenados por data asc */
    List<Order> findByStatusInOrderByCreatedAtAsc(List<StatusPedidoEnum> statuses);

    /** Busca pedidos por intervalo de datas */
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /** Busca pedidos de um usuário por intervalo de datas */
    List<Order> findByUsuarioAndCreatedAtBetween(Usuario usuario, LocalDateTime start, LocalDateTime end);

    /** Busca pedidos criados após uma data */
    List<Order> findByCreatedAtAfter(LocalDateTime date);

    /** Busca pedidos de um usuário criados após uma data */
    List<Order> findByUsuarioAndCreatedAtAfter(Usuario usuario, LocalDateTime date);

    /** Busca pedidos por valor total */
    List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);

    /** Busca pedidos de um usuário por valor total */
    List<Order> findByUsuarioAndTotalBetween(Usuario usuario, BigDecimal minTotal, BigDecimal maxTotal);

    /** Busca pedidos entregues entre duas datas (deliveredAt) */
    List<Order> findByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);

    /** Busca pedido por ID e usuário */
    Optional<Order> findByIdAndUsuario(Long id, Usuario usuario);

    /** Busca pedido por código */
    Optional<Order> findByCodigoPedido(String codigoPedido);

    // ===== PAGINAÇÃO =====
    /** Busca pedidos paginados por status */
    Page<Order> findByStatus(StatusPedidoEnum status, Pageable pageable);

    /** Busca pedidos paginados de um usuário */
    Page<Order> findByUsuario(Usuario usuario, Pageable pageable);

    /** Busca pedidos paginados por intervalo de datas */
    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** Busca pedidos paginados de um usuário por intervalo de datas */
    Page<Order> findByUsuarioAndCreatedAtBetween(Usuario usuario, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** Busca pedidos paginados por valor total */
    Page<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal, Pageable pageable);

    /** Busca pedidos paginados de um usuário por valor total */
    Page<Order> findByUsuarioAndTotalBetween(Usuario usuario, BigDecimal minTotal, BigDecimal maxTotal, Pageable pageable);

    // ===== CONTAGEM =====
    /** Conta pedidos por status */
    long countByStatus(StatusPedidoEnum status);

    /** Conta pedidos de um usuário */
    long countByUsuario(Usuario usuario);

    /** Conta pedidos de um usuário por status */
    long countByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    /** Conta pedidos criados entre duas datas */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /** Conta pedidos com um determinado status dentro do período */
    long countByStatusAndCreatedAtBetween(StatusPedidoEnum status, LocalDateTime start, LocalDateTime end);

    /** Conta pedidos que NÃO são do status fornecido dentro do período */
    long countByStatusNotAndCreatedAtBetween(StatusPedidoEnum status, LocalDateTime start, LocalDateTime end);

    // ===== DASHBOARD E ANÁLISES =====
    /** Busca últimos N pedidos de um usuário (mais recentes) */
    // Use findByUsuarioOrderByCreatedAtDesc(Usuario usuario) + Pageable para limitar

    /** Busca pedidos do período ordenados por createdAt asc */
    List<Order> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime start, LocalDateTime end);

    /** Soma o total dos pedidos no período (exclui pedidos CANCELADO). Retorna 0 se não houver registros. */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status <> com.basilios.basilios.core.enums.StatusPedidoEnum.CANCELADO")
    BigDecimal sumTotalByCreatedAtBetweenExcludeCancelled(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** Busca pares dispatchedAt e deliveredAt de pedidos ENTREGUES no período */
    @Query("SELECT o.dispatchedAt, o.deliveredAt FROM Order o WHERE o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.ENTREGUE AND o.dispatchedAt IS NOT NULL AND o.deliveredAt IS NOT NULL AND o.createdAt BETWEEN :start AND :end")
    List<Object[]> findDispatchedAndDeliveredTimesOfDeliveredOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
