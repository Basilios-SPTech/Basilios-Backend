package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "product_combo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductCombo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Produto é obrigatório")
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    @NotNull(message = "Combo é obrigatório")
    @ToString.Exclude
    private Combo combo;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String customization;

    /**
     * Validação antes de persistir
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        // Copia o nome do produto para histórico
        if (product != null && productName == null) {
            productName = product.getName();
        }
    }

    @Override
    public String toString() {
        return "ProductCombo{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", customization='" + customization + '\'' +
                '}';
    }
}