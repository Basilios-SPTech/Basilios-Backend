package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {

    /**
     * Busca combos ativos
     */
    List<Combo> findByIsActiveTrue();

    /**
     * Busca combos inativos
     */
    List<Combo> findByIsActiveFalse();

    /**
     * Busca combos ativos paginados
     */
    Page<Combo> findByIsActiveTrue(Pageable pageable);

    /**
     * Busca combo por nome (ignora case)
     */
    Optional<Combo> findByNameIgnoreCase(String name);

    /**
     * Busca combos por nome parcial (ignora case)
     */
    List<Combo> findByNameContainingIgnoreCase(String name);

    /**
     * Busca combos ativos por nome parcial
     */
    List<Combo> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Verifica se existe combo com o nome
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca combos por faixa de preço
     */
    List<Combo> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca combos ativos por faixa de preço
     */
    List<Combo> findByPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca combos com preço menor ou igual
     */
    List<Combo> findByPriceLessThanEqual(BigDecimal maxPrice);

    /**
     * Busca combos ativos com preço menor ou igual
     */
    List<Combo> findByPriceLessThanEqualAndIsActiveTrue(BigDecimal maxPrice);

    /**
     * Busca combos ordenados por preço (crescente)
     */
    List<Combo> findByIsActiveTrueOrderByPriceAsc();

    /**
     * Busca combos ordenados por preço (decrescente)
     */
    List<Combo> findByIsActiveTrueOrderByPriceDesc();

    /**
     * Busca combos ordenados por nome
     */
    List<Combo> findByIsActiveTrueOrderByNameAsc();

    /**
     * Busca combos mais recentes
     */
    @Query("SELECT c FROM Combo c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Combo> findRecentActive(Pageable pageable);

    /**
     * Busca combos que contêm um produto específico
     */
    @Query("SELECT DISTINCT c FROM Combo c " +
            "JOIN c.productCombos pc " +
            "WHERE pc.product.id = :productId AND c.isActive = true")
    List<Combo> findByProductId(@Param("productId") Long productId);

    /**
     * Busca combos onde TODOS os produtos estão ativos
     */
    @Query("SELECT c FROM Combo c WHERE c.isActive = true " +
            "AND NOT EXISTS (" +
            "  SELECT pc FROM ProductCombo pc " +
            "  WHERE pc.combo = c AND pc.product.isPaused = true" +
            ")")
    List<Combo> findFullyAvailable();

    /**
     * Busca combos vantajosos (preço do combo menor que soma dos produtos)
     */
    @Query("SELECT c FROM Combo c " +
            "JOIN c.productCombos pc " +
            "WHERE c.isActive = true " +
            "GROUP BY c " +
            "HAVING c.price < SUM(pc.product.price * pc.quantity)")
    List<Combo> findAdvantageous();

    /**
     * Conta combos ativos
     */
    long countByIsActiveTrue();

    /**
     * Conta combos inativos
     */
    long countByIsActiveFalse();

    /**
     * Busca combo com seus produtos (fetch eager)
     */
    @Query("SELECT c FROM Combo c " +
            "LEFT JOIN FETCH c.productCombos pc " +
            "LEFT JOIN FETCH pc.product " +
            "WHERE c.id = :id")
    Optional<Combo> findByIdWithProducts(@Param("id") Long id);

    /**
     * Estatísticas dos combos
     */
    @Query("SELECT " +
            "COUNT(c), " +
            "SUM(CASE WHEN c.isActive = true THEN 1 ELSE 0 END), " +
            "AVG(c.price), " +
            "MIN(c.price), " +
            "MAX(c.price) " +
            "FROM Combo c")
    Object[] getComboStatistics();
}