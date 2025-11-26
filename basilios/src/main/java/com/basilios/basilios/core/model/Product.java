package com.basilios.basilios.core.model;

import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 255, message = "Nome do produto deve ter no máximo 255 caracteres")
    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category; // BURGER, SIDE, DRINK, etc

    @Enumerated(EnumType.STRING)
    private ProductSubcategory subcategory; // BEEF, CHICKEN, etc

    @ElementCollection
    @CollectionTable(name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>(); // ["ARTESANAL", "PICANTE"]

    // Relacionamento Many-to-Many com Promotion (lado inverso)
    @ManyToMany(mappedBy = "products")
    @Builder.Default
    @ToString.Exclude
    private List<Promotion> promotions = new ArrayList<>();

    // Relacionamento Many-to-Many com Ingredient (através de IngredientProduct)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<IngredientProduct> productIngredients = new ArrayList<>();

    // Relacionamento Many-to-Many com Order (através de ProductOrder)
    // Removido cascade/orphanRemoval: Order é o aggregate root dos itens de pedido
    @OneToMany(mappedBy = "product")
    @Builder.Default
    @ToString.Exclude
    private List<ProductOrder> productOrders = new ArrayList<>();

    // Relacionamento Many-to-Many com Combo (através de ProductCombo)
    @OneToMany(mappedBy = "product")
    @Builder.Default
    @ToString.Exclude
    private List<ProductCombo> productCombos = new ArrayList<>();

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço deve ser maior ou igual a zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPaused = false;

    // Métodos utilitários

    /**
     * Verifica se o produto está ativo
     */
    public boolean isActive() {
        return !isPaused;
    }

    /**
     * Pausa o produto (desativa)
     */
    public void pause() {
        this.isPaused = true;
    }

    /**
     * Ativa o produto
     */
    public void activate() {
        this.isPaused = false;
    }

    /**
     * Alterna o status do produto
     */
    public void toggleStatus() {
        this.isPaused = !this.isPaused;
    }

    /**
     * Retorna o preço final considerando promoções vigentes
     */
    public BigDecimal getFinalPrice() {
        if (promotions == null || promotions.isEmpty()) {
            return price;
        }

        // Busca a melhor promoção vigente (maior desconto)
        return promotions.stream()
                .filter(Promotion::isCurrent)
                .map(promo -> promo.calculateDiscountedPrice(price))
                .min(BigDecimal::compareTo)
                .orElse(price);
    }

    /**
     * Verifica se o produto está em promoção
     */
    public boolean isOnPromotion() {
        if (promotions == null || promotions.isEmpty()) {
            return false;
        }
        return promotions.stream().anyMatch(Promotion::isCurrent);
    }

    /**
     * Retorna a promoção vigente com maior desconto
     */
    public Promotion getBestCurrentPromotion() {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }

        return promotions.stream()
                .filter(Promotion::isCurrent)
                .min((p1, p2) -> {
                    BigDecimal price1 = p1.calculateDiscountedPrice(price);
                    BigDecimal price2 = p2.calculateDiscountedPrice(price);
                    return price1.compareTo(price2);
                })
                .orElse(null);
    }

    /**
     * Adiciona um ingrediente ao produto
     */
    public void addIngredient(Ingredient ingredient, Integer quantity, String measurementUnit) {
        IngredientProduct ip = new IngredientProduct();
        ip.setProduct(this);
        ip.setIngredient(ingredient);
        ip.setQuantity(quantity);
        ip.setMeasurementUnit(measurementUnit);
        productIngredients.add(ip);
        ingredient.getProductIngredients().add(ip);
    }

    /**
     * Remove um ingrediente do produto
     */
    public void removeIngredient(Ingredient ingredient) {
        if (ingredient == null) return;

        Iterator<IngredientProduct> it = productIngredients.iterator();
        while (it.hasNext()) {
            IngredientProduct ip = it.next();
            if (ip.getIngredient() != null && ip.getIngredient().equals(ingredient)) {
                // remove dos dois lados e zera referências da entidade de junção
                it.remove();
                ingredient.getProductIngredients().remove(ip);
                ip.setProduct(null);
                ip.setIngredient(null);
            }
        }
    }

    /**
     * Remove todos os ingredientes
     */
    public void clearIngredients() {
        for (IngredientProduct ip : new ArrayList<>(productIngredients)) {
            Ingredient ingredient = ip.getIngredient();
            if (ingredient != null) {
                ingredient.getProductIngredients().remove(ip);
            }
            ip.setProduct(null);
            ip.setIngredient(null);
        }
        productIngredients.clear();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", isPaused=" + isPaused +
                ", ingredientsCount=" + (productIngredients != null ? productIngredients.size() : 0) +
                ", promotionsCount=" + (promotions != null ? promotions.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}