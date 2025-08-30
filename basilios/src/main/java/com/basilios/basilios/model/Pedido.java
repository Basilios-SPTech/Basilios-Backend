package com.basilios.basilios.model;

import com.basilios.basilios.enums.MetodoPagamentoEnum;
import com.basilios.basilios.enums.StatusPedidoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pedido_cliente"))
    @NotNull(message = "Cliente é obrigatório")
    private Cliente cliente;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "produtos_ids", nullable = false, columnDefinition = "json")
    @NotNull(message = "Lista de produtos é obrigatória")
    private List<Long> produtosIds;

    @NotNull(message = "Preço total é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço total deve ser maior ou igual a zero")
    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    @NotNull(message = "Método de pagamento é obrigatório")
    private MetodoPagamentoEnum metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPedidoEnum status = StatusPedidoEnum.PENDENTE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "token_pedido")
    private Integer tokenPedido;

    // Construtores
    public Pedido() {}

    public Pedido(Cliente cliente, List<Long> produtosIds, BigDecimal precoTotal, MetodoPagamentoEnum metodoPagamento) {
        this.cliente = cliente;
        this.produtosIds = produtosIds;
        this.precoTotal = precoTotal;
        this.metodoPagamento = metodoPagamento;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }

    public BigDecimal getPrecoTotal() {
        return precoTotal;
    }

    public void setPrecoTotal(BigDecimal precoTotal) {
        this.precoTotal = precoTotal;
    }

    public MetodoPagamentoEnum getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamentoEnum metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public StatusPedidoEnum getStatus() {
        return status;
    }

    public void setStatus(StatusPedidoEnum status) {
        // Verifica se a transição é válida
        if (this.status != null && !this.status.podeTransicionarPara(status)) {
            throw new IllegalStateException("Não é possível alterar status de " + this.status + " para " + status);
        }

        this.status = status;

        // Define completed_at quando o pedido é entregue ou cancelado
        if (status.isFinal()) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTokenPedido() {
        return tokenPedido;
    }

    public void setTokenPedido(Integer tokenPedido) {
        this.tokenPedido = tokenPedido;
    }

    // Métodos utilitários
    public boolean isPendente() {
        return status == StatusPedidoEnum.PENDENTE;
    }

    public boolean isConfirmado() {
        return status == StatusPedidoEnum.CONFIRMADO;
    }

    public boolean isPreparando() {
        return status == StatusPedidoEnum.PREPARANDO;
    }

    public boolean isDespachado() {
        return status == StatusPedidoEnum.DESPACHADO;
    }

    public boolean isEntregue() {
        return status == StatusPedidoEnum.ENTREGUE;
    }

    public boolean isCancelado() {
        return status == StatusPedidoEnum.CANCELADO;
    }

    public boolean isCompleto() {
        return status != null && status.isFinal();
    }

    public void confirmar() {
        setStatus(StatusPedidoEnum.CONFIRMADO);
    }

    public void iniciarPreparo() {
        setStatus(StatusPedidoEnum.PREPARANDO);
    }

    public void despachar() {
        setStatus(StatusPedidoEnum.DESPACHADO);
    }

    public void entregar() {
        setStatus(StatusPedidoEnum.ENTREGUE);
    }

    public void cancelar() {
        setStatus(StatusPedidoEnum.CANCELADO);
    }

    // toString
    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente=" + (cliente != null ? cliente.getIdCliente() : null) +
                ", produtosIds=" + produtosIds +
                ", precoTotal=" + precoTotal +
                ", metodoPagamento=" + metodoPagamento +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", completedAt=" + completedAt +
                ", tokenPedido=" + tokenPedido +
                '}';
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pedido pedido = (Pedido) o;

        return id != null ? id.equals(pedido.id) : pedido.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}