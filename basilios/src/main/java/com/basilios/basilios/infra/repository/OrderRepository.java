package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Busca pedidos de um usuário ordenados por data (mais recentes primeiro)
     */
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
    @Query("SELECT o FROM Order o WHERE o.usuario = :usuario ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Busca pedidos pendentes
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDENTE' ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders();

    /**
     * Busca pedidos em andamento (confirmado, preparando, despachado)
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('CONFIRMADO', 'PREPARANDO', 'DESPACHADO') ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders();
}