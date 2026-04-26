package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_order_adicional")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductOrderAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_order_id", nullable = false)
    @NotNull
    @ToString.Exclude
    private ProductOrder productOrder;

    /**
     * Referência ao adicional original (mantida para histórico).
     * Guardamos como Long para evitar problema caso o adicional seja removido.
     */
    @Column(name = "adicional_id")
    private Long adicionalId;

    @NotBlank(message = "Nome do adicional é obrigatório")
    @Column(name = "adicional_name", nullable = false, length = 255)
    private String adicionalName;

    @NotNull
    @DecimalMin(value = "0.00", message = "Preço unitário deve ser maior ou igual a zero")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
