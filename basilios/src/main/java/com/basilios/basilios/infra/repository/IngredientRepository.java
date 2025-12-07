package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * Busca ingrediente por nome (ignora case)
     */
    Optional<Ingredient> findByNameIgnoreCase(String name);

    /**
     * Busca ingredientes por nome parcial (ignora case)
     */
    List<Ingredient> findByNameContainingIgnoreCase(String name);

    /**
     * Verifica se existe ingrediente com o nome
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca todos os ingredientes ordenados por nome
     */
    List<Ingredient> findAllByOrderByNameAsc();

    /**
     * Busca ingredientes que são usados em um produto específico
     */
    @Query("SELECT DISTINCT i FROM Ingredient i JOIN i.productIngredients pi WHERE pi.product.id = :productId")
    List<Ingredient> findByProductId(@Param("productId") Long productId);

    /**
     * Busca ingredientes que NÃO são usados em nenhum produto
     */
    @Query("SELECT i FROM Ingredient i WHERE i.productIngredients IS EMPTY")
    List<Ingredient> findUnusedIngredients();

    /**
     * Busca ingredientes mais usados (ordenado por quantidade de produtos)
     */
    @Query("SELECT i, COUNT(pi) as usageCount FROM Ingredient i " +
            "JOIN i.productIngredients pi " +
            "GROUP BY i " +
            "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedIngredients();

    /**
     * Conta quantos produtos usam um ingrediente específico
     */
    @Query("SELECT COUNT(DISTINCT pi.product) FROM IngredientProduct pi " +
            "WHERE pi.ingredient.id = :ingredientId")
    long countProductsUsingIngredient(@Param("ingredientId") Long ingredientId);

    /**
     * Busca ingredientes usados em produtos ativos
     */
    @Query("SELECT DISTINCT i FROM Ingredient i " +
            "JOIN i.productIngredients pi " +
            "WHERE pi.product.isPaused = false")
    List<Ingredient> findUsedInActiveProducts();
}