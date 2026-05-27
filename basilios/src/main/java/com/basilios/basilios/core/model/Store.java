package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@SQLDelete(sql = "UPDATE stores SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false)
    private String address;

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @NotNull
    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee = new BigDecimal("5.00");

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
