package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"productOrders", "productOrders.product"})
    List<Order> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    List<Order> findByUsuario(Usuario usuario);

    List<Order> findByStatus(StatusPedidoEnum status);

    @Query("SELECT o FROM Order o WHERE o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.PENDENTE ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders();

    @Query("SELECT o FROM Order o WHERE o.status IN (com.basilios.basilios.core.enums.StatusPedidoEnum.CONFIRMADO, com.basilios.basilios.core.enums.StatusPedidoEnum.PREPARANDO, com.basilios.basilios.core.enums.StatusPedidoEnum.DESPACHADO) ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders();

    List<Order> findByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    List<Order> findByUsuarioAndCreatedAtAfter(Usuario usuario, LocalDateTime date);

    long countByUsuario(Usuario usuario);

    long countByUsuarioAndStatus(Usuario usuario, StatusPedidoEnum status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(StatusPedidoEnum status, LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.ENTREGUE")
    BigDecimal sumTotalByCreatedAtBetweenEntregue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.basilios.basilios.core.enums.StatusPedidoEnum.CANCELADO AND o.createdAt BETWEEN :start AND :end")
    long countCancelledOrdersByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Order> findByStatusAndCreatedAtBetween(StatusPedidoEnum status, LocalDateTime start, LocalDateTime end);
}
