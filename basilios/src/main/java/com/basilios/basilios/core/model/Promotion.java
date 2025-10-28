package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    // Relacionamento Many-to-Many com Product
    @ManyToMany
    @JoinTable(
            name = "promotion_product",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();

    @DecimalMin(value = "0.0", message = "Desconto percentual deve ser maior ou igual a 0")
    @DecimalMax(value = "100.0", message = "Desconto percentual deve ser menor ou igual a 100")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.00", message = "Valor fixo de desconto deve ser maior ou igual a zero")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @NotNull(message = "Data de início é obrigatória")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Data de término é obrigatória")
    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos utilitários

    /**
     * Verifica se a promoção está vigente (ativa e dentro do período)
     */
    public boolean isCurrent() {
        if (!isActive) return false;
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Verifica se a promoção está expirada
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Verifica se a promoção está agendada (ainda não começou)
     */
    public boolean isScheduled() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Ativa a promoção
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desativa a promoção
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Alterna o status da promoção
     */
    public void toggleStatus() {
        this.isActive = !this.isActive;
    }

    /**
     * Calcula o preço com desconto para um produto específico
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return originalPrice;
        }

        BigDecimal discount = BigDecimal.ZERO;

        // Desconto percentual tem prioridade
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            discount = originalPrice
                    .multiply(discountPercentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        // Se não houver percentual, usa valor fixo
        else if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            discount = discountAmount;
        }

        BigDecimal finalPrice = originalPrice.subtract(discount);

        // Garante que o preço não fique negativo
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }

    /**
     * Calcula o valor do desconto para um produto específico
     */
    public BigDecimal calculateDiscount(BigDecimal originalPrice) {
        if (originalPrice == null) return BigDecimal.ZERO;

        BigDecimal discountedPrice = calculateDiscountedPrice(originalPrice);
        return originalPrice.subtract(discountedPrice);
    }

    /**
     * Valida se o período é válido
     */
    public boolean isValidPeriod() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }

    /**
     * Adiciona um produto à promoção
     */
    public void addProduct(Product product) {
        products.add(product);
        product.getPromotions().add(this);
    }

    /**
     * Remove um produto da promoção
     */
    public void removeProduct(Product product) {
        products.remove(product);
        product.getPromotions().remove(this);
    }

    /**
     * Remove todos os produtos
     */
    public void clearProducts() {
        for (Product product : new ArrayList<>(products)) {
            removeProduct(product);
        }
    }

    // Validação customizada
    @PrePersist
    @PreUpdate
    private void validateData() {
        if (!isValidPeriod()) {
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início");
        }

        if ((discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) &&
                (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0)) {
            throw new IllegalArgumentException("Promoção deve ter desconto percentual ou valor fixo");
        }

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0 &&
                discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Promoção deve ter apenas um tipo de desconto (percentual OU valor fixo)");
        }
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", discountAmount=" + discountAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", productsCount=" + (products != null ? products.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}