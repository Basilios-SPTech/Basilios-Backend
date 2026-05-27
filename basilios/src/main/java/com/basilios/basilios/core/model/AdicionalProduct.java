package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "adicional_product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "adicional_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AdicionalProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adicional_id", nullable = false)
    @NotNull
    @ToString.Exclude
    private Adicional adicional;
}
