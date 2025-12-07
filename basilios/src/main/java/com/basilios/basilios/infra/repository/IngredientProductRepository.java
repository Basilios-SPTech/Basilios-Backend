package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Ingredient;
import com.basilios.basilios.core.model.IngredientProduct;
import com.basilios.basilios.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientProductRepository extends JpaRepository<IngredientProduct, Long> {

    /**
     * Busca todos os ingredientes de um produto
     */
    List<IngredientProduct> findByProduct(Product product);

    /**
     * Busca todos os ingredientes de um produto por ID
     */
    List<IngredientProduct> findByProductId(Long productId);

    /**
     * Busca todos os produtos que contêm um ingrediente
     */
    List<IngredientProduct> findByIngredient(Ingredient ingredient);

    /**
     * Busca todos os produtos que contêm um ingrediente por ID
     */
    List<IngredientProduct> findByIngredientId(Long ingredientId);

    /**
     * Busca uma relação específica produto-ingrediente
     */
    Optional<IngredientProduct> findByProductAndIngredient(Product product, Ingredient ingredient);

    /**
     * Busca uma relação específica produto-ingrediente por IDs
     */
    @Query("SELECT ip FROM IngredientProduct ip WHERE ip.product.id = :productId AND ip.ingredient.id = :ingredientId")
    Optional<IngredientProduct> findByProductIdAndIngredientId(
            @Param("productId") Long productId,
            @Param("ingredientId") Long ingredientId
    );

    /**
     * Verifica se um produto possui um ingrediente específico
     */
    boolean existsByProductAndIngredient(Product product, Ingredient ingredient);

    /**
     * Verifica se um produto possui um ingrediente específico por IDs
     */
    @Query("SELECT CASE WHEN COUNT(ip) > 0 THEN true ELSE false END " +
            "FROM IngredientProduct ip " +
            "WHERE ip.product.id = :productId AND ip.ingredient.id = :ingredientId")
    boolean existsByProductIdAndIngredientId(
            @Param("productId") Long productId,
            @Param("ingredientId") Long ingredientId
    );

    /**
     * Deleta todos os ingredientes de um produto
     */
    void deleteByProduct(Product product);

    /**
     * Deleta todos os ingredientes de um produto por ID
     */
    void deleteByProductId(Long productId);

    /**
     * Deleta um ingrediente específico de um produto
     */
    void deleteByProductAndIngredient(Product product, Ingredient ingredient);

    /**
     * Conta quantos ingredientes um produto tem
     */
    long countByProduct(Product product);

    /**
     * Conta quantos ingredientes um produto tem por ID
     */
    long countByProductId(Long productId);

    /**
     * Busca ingredientes de um produto ordenados por nome
     */
    @Query("SELECT ip FROM IngredientProduct ip " +
            "WHERE ip.product.id = :productId " +
            "ORDER BY ip.ingredient.name ASC")
    List<IngredientProduct> findByProductIdOrderedByName(@Param("productId") Long productId);

    /**
     * Busca produtos que usam um ingrediente específico (apenas ativos)
     */
    @Query("SELECT ip FROM IngredientProduct ip " +
            "WHERE ip.ingredient.id = :ingredientId AND ip.product.isPaused = false")
    List<IngredientProduct> findActiveProductsByIngredientId(@Param("ingredientId") Long ingredientId);
}