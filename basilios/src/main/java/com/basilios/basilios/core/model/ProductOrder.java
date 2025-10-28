package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Produto é obrigatório")
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Pedido é obrigatório")
    @ToString.Exclude
    private Order order;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço unitário deve ser maior ou igual a zero")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Subtotal é obrigatório")
    @DecimalMin(value = "0.00", message = "Subtotal deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Builder.Default
    @Column(name = "had_promotion")
    private Boolean hadPromotion = false;

    @Column(name = "promotion_name", length = 255)
    private String promotionName;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Métodos utilitários

    /**
     * Calcula o subtotal baseado na quantidade e preço unitário
     */
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }

    /**
     * Calcula o desconto total aplicado (se houver)
     */
    public BigDecimal getTotalDiscount() {
        if (hadPromotion && originalPrice != null && unitPrice != null) {
            BigDecimal discountPerUnit = originalPrice.subtract(unitPrice);
            return discountPerUnit.multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Retorna o valor total economizado em percentual
     */
    public BigDecimal getDiscountPercentage() {
        if (hadPromotion && originalPrice != null && unitPrice != null &&
                originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = originalPrice.subtract(unitPrice);
            return discount.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Validação antes de persistir
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço unitário inválido");
        }

        // Calcula subtotal automaticamente
        calculateSubtotal();

        // Copia o nome do produto no momento do pedido (para histórico)
        if (product != null && productName == null) {
            productName = product.getName();
        }

        // Valida dados de promoção
        if (hadPromotion == null) {
            hadPromotion = false;
        }
    }

    @Override
    public String toString() {
        return "ProductOrder{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                ", hadPromotion=" + hadPromotion +
                ", observations='" + observations + '\'' +
                '}';
    }
}