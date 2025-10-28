package com.basilios.basilios.core.model;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @NotNull(message = "Usuário é obrigatório")
    @ToString.Exclude
    private Usuario usuario;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ProductOrder> productOrders = new ArrayList<>();

    @NotNull(message = "Total é obrigatório")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_entrega_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_endereco"))
    @NotNull(message = "Endereço de entrega é obrigatório")
    @ToString.Exclude
    private Address addressEntrega;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatusPedidoEnum status = StatusPedidoEnum.PENDENTE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "preparing_at")
    private LocalDateTime preparingAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(columnDefinition = "TEXT")
    private String observations;

    // Métodos de gerenciamento de items

    /**
     * Adiciona um produto ao pedido
     */
    public void addProduct(Product product, Integer quantity, BigDecimal unitPrice) {
        ProductOrder po = ProductOrder.builder()
                .order(this)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .productName(product.getName())
                .build();

        // Verifica se produto está em promoção
        if (product.isOnPromotion()) {
            Promotion promo = product.getBestCurrentPromotion();
            po.setHadPromotion(true);
            po.setPromotionName(promo.getTitle());
            po.setOriginalPrice(product.getPrice());
        }

        po.calculateSubtotal();
        productOrders.add(po);

    }

    /**
     * Remove um produto do pedido
     */
    public void removeProduct(ProductOrder productOrder) {
        if (productOrder == null) {
            return;
        }

        productOrders.remove(productOrder);

        // NOTA: não atualizamos o lado do Product aqui. O Order é o aggregate root
        // responsável pelos itens; manter atualizações do lado do Product aumenta
        // risco de inconsistência se o campo productOrders for removido do Product.

        productOrder.setOrder(null);
    }

    /**
     * Limpa todos os produtos
     */
    public void clearProducts() {
        // Remove de forma segura, mantendo ambos os lados consistentes
        for (ProductOrder po : new ArrayList<>(productOrders)) {
            removeProduct(po);
        }
    }

    /**
     * Calcula o total do pedido
     */
    public void calculateTotal() {
        // Calcula subtotal dos produtos
        this.subtotal = productOrders.stream()
                .map(ProductOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Inicializa valores se null
        if (deliveryFee == null) deliveryFee = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;

        // Total = Subtotal + Taxa de entrega - Desconto
        this.total = subtotal
                .add(deliveryFee)
                .subtract(discount);

        // Garante que não fique negativo
        if (this.total.compareTo(BigDecimal.ZERO) < 0) {
            this.total = BigDecimal.ZERO;
        }
    }

    /**
     * Retorna quantidade total de items
     */
    public int getTotalItems() {
        return productOrders.stream()
                .mapToInt(ProductOrder::getQuantity)
                .sum();
    }

    /**
     * Retorna desconto total aplicado nas promoções
     */
    public BigDecimal getTotalPromotionDiscount() {
        return productOrders.stream()
                .map(ProductOrder::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Métodos de status

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

    /**
     * Confirma o pedido
     */
    public void confirmar() {
        if (!isPendente()) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser confirmados");
        }
        this.status = StatusPedidoEnum.CONFIRMADO;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * Inicia preparo do pedido
     */
    public void iniciarPreparo() {
        if (!isConfirmado()) {
            throw new IllegalStateException("Apenas pedidos confirmados podem ir para preparo");
        }
        this.status = StatusPedidoEnum.PREPARANDO;
        this.preparingAt = LocalDateTime.now();
    }

    /**
     * Despacha o pedido
     */
    public void despachar() {
        if (!isPreparando()) {
            throw new IllegalStateException("Apenas pedidos em preparo podem ser despachados");
        }
        this.status = StatusPedidoEnum.DESPACHADO;
        this.dispatchedAt = LocalDateTime.now();
    }

    /**
     * Marca pedido como entregue
     */
    public void entregar() {
        if (!isDespachado()) {
            throw new IllegalStateException("Apenas pedidos despachados podem ser entregues");
        }
        this.status = StatusPedidoEnum.ENTREGUE;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Cancela o pedido
     */
    public void cancelar(String motivo) {
        if (isEntregue()) {
            throw new IllegalStateException("Pedidos entregues não podem ser cancelados");
        }
        this.status = StatusPedidoEnum.CANCELADO;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = motivo;
    }

    /**
     * Validação antes de persistir
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (productOrders.isEmpty()) {
            throw new IllegalStateException("Pedido deve ter pelo menos um produto");
        }

        // Calcula total automaticamente
        calculateTotal();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", status=" + status +
                ", total=" + total +
                ", subtotal=" + subtotal +
                ", deliveryFee=" + deliveryFee +
                ", discount=" + discount +
                ", itemsCount=" + (productOrders != null ? productOrders.size() : 0) +
                ", createdAt=" + createdAt +
                '}';
    }
}