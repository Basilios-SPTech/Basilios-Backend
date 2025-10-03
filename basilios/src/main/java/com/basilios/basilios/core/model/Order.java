package com.basilios.basilios.core.model;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_usuario"))
    @NotNull
    @ToString.Exclude
    private Usuario usuario;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<OrderItem> items;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_entrega_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_endereco"))
    @NotNull
    @ToString.Exclude
    private Endereco enderecoEntrega;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatusPedidoEnum status = StatusPedidoEnum.PENDENTE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
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

    public void confirmar() {
        this.status = StatusPedidoEnum.CONFIRMADO;
    }

    public void iniciarPreparo() {
        this.status = StatusPedidoEnum.PREPARANDO;
    }

    public void despachar() {
        this.status = StatusPedidoEnum.DESPACHADO;
    }

    public void entregar() {
        this.status = StatusPedidoEnum.ENTREGUE;
    }

    public void cancelar() {
        this.status = StatusPedidoEnum.CANCELADO;
    }
}