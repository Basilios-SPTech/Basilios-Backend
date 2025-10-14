package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "combo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do combo é obrigatório")
    @Size(max = 255, message = "Nome do combo deve ter no máximo 255 caracteres")
    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<ProductCombo> productCombos = new HashSet<>();

    @NotNull(message = "Preço do combo é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço do combo deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos utilitários

    /**
     * Adiciona um produto ao combo
     */
    public void addProduct(Product product, Integer quantity) {
        ProductCombo pc = ProductCombo.builder()
                .combo(this)
                .product(product)
                .quantity(quantity)
                .build();

        productCombos.add(pc);
        product.getProductCombos().add(pc);
    }

    /**
     * Remove um produto do combo
     */
    public void removeProduct(Product product) {
        productCombos.removeIf(pc -> pc.getProduct().equals(product));
        product.getProductCombos().removeIf(pc -> pc.getCombo().equals(this));
    }

    /**
     * Limpa todos os produtos do combo
     */
    public void clearProducts() {
        for (ProductCombo pc : new HashSet<>(productCombos)) {
            removeProduct(pc.getProduct());
        }
    }

    /**
     * Calcula o preço total dos produtos individuais (sem combo)
     */
    public BigDecimal calculateIndividualPrice() {
        return productCombos.stream()
                .map(pc -> {
                    BigDecimal productPrice = pc.getProduct().getPrice();
                    return productPrice.multiply(new BigDecimal(pc.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula a economia do combo (diferença entre preço individual e preço do combo)
     */
    public BigDecimal calculateSavings() {
        BigDecimal individualPrice = calculateIndividualPrice();
        return individualPrice.subtract(price);
    }

    /**
     * Calcula o percentual de desconto do combo
     */
    public BigDecimal calculateDiscountPercentage() {
        BigDecimal individualPrice = calculateIndividualPrice();
        if (individualPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal savings = calculateSavings();
        return savings.divide(individualPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Verifica se o combo é vantajoso (preço do combo menor que soma dos produtos)
     */
    public boolean isAdvantageous() {
        return calculateSavings().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Retorna quantidade total de items no combo
     */
    public int getTotalItems() {
        return productCombos.stream()
                .mapToInt(ProductCombo::getQuantity)
                .sum();
    }

    /**
     * Verifica se todos os produtos do combo estão disponíveis
     */
    public boolean isAvailable() {
        if (!isActive) return false;

        return productCombos.stream()
                .allMatch(pc -> pc.getProduct() != null && pc.getProduct().isActive());
    }

    /**
     * Ativa o combo
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desativa o combo
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Alterna o status do combo
     */
    public void toggleStatus() {
        this.isActive = !this.isActive;
    }

    /**
     * Validação antes de persistir
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (productCombos.isEmpty()) {
            throw new IllegalStateException("Combo deve ter pelo menos um produto");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do combo deve ser maior que zero");
        }
    }

    @Override
    public String toString() {
        return "Combo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", isActive=" + isActive +
                ", productsCount=" + (productCombos != null ? productCombos.size() : 0) +
                ", totalItems=" + getTotalItems() +
                ", createdAt=" + createdAt +
                '}';
    }
}