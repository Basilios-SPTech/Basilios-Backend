package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Combo;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.ProductCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductComboRepository extends JpaRepository<ProductCombo, Long> {

    /**
     * Busca todos os produtos de um combo
     */
    List<ProductCombo> findByCombo(Combo combo);

    /**
     * Busca todos os produtos de um combo por ID
     */
    List<ProductCombo> findByComboId(Long comboId);

    /**
     * Busca todos os combos que contêm um produto
     */
    List<ProductCombo> findByProduct(Product product);

    /**
     * Busca todos os combos que contêm um produto por ID
     */
    List<ProductCombo> findByProductId(Long productId);

    /**
     * Busca uma relação específica produto-combo
     */
    Optional<ProductCombo> findByProductAndCombo(Product product, Combo combo);

    /**
     * Busca uma relação específica produto-combo por IDs
     */
    @Query("SELECT pc FROM ProductCombo pc " +
            "WHERE pc.product.id = :productId AND pc.combo.id = :comboId")
    Optional<ProductCombo> findByProductIdAndComboId(
            @Param("productId") Long productId,
            @Param("comboId") Long comboId
    );

    /**
     * Verifica se um combo possui um produto específico
     */
    boolean existsByComboAndProduct(Combo combo, Product product);

    /**
     * Verifica se um combo possui um produto específico por IDs
     */
    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END " +
            "FROM ProductCombo pc " +
            "WHERE pc.combo.id = :comboId AND pc.product.id = :productId")
    boolean existsByComboIdAndProductId(
            @Param("comboId") Long comboId,
            @Param("productId") Long productId
    );

    /**
     * Deleta todos os produtos de um combo
     */
    void deleteByCombo(Combo combo);

    /**
     * Deleta todos os produtos de um combo por ID
     */
    void deleteByComboId(Long comboId);

    /**
     * Deleta um produto específico de um combo
     */
    void deleteByComboAndProduct(Combo combo, Product product);

    /**
     * Conta quantos produtos um combo tem
     */
    long countByCombo(Combo combo);

    /**
     * Conta quantos produtos um combo tem por ID
     */
    long countByComboId(Long comboId);

    /**
     * Conta quantos combos usam um produto
     */
    long countByProduct(Product product);

    /**
     * Conta quantos combos usam um produto por ID
     */
    long countByProductId(Long productId);

    /**
     * Busca produtos de um combo ordenados por nome
     */
    @Query("SELECT pc FROM ProductCombo pc " +
            "WHERE pc.combo.id = :comboId " +
            "ORDER BY pc.product.name ASC")
    List<ProductCombo> findByComboIdOrderedByProductName(@Param("comboId") Long comboId);

    /**
     * Busca combos ativos que usam um produto específico
     */
    @Query("SELECT pc FROM ProductCombo pc " +
            "WHERE pc.product.id = :productId AND pc.combo.isActive = true")
    List<ProductCombo> findActiveCombosByProductId(@Param("productId") Long productId);

    /**
     * Busca produtos de combos ativos
     */
    @Query("SELECT pc FROM ProductCombo pc " +
            "WHERE pc.combo.id = :comboId " +
            "AND pc.combo.isActive = true " +
            "AND pc.product.isPaused = false")
    List<ProductCombo> findAvailableProductsByComboId(@Param("comboId") Long comboId);

    /**
     * Calcula quantidade total de items em um combo
     */
    @Query("SELECT SUM(pc.quantity) FROM ProductCombo pc WHERE pc.combo.id = :comboId")
    Integer getTotalItemsByComboId(@Param("comboId") Long comboId);

    /**
     * Busca produtos mais usados em combos
     */
    @Query("SELECT pc.product, COUNT(pc) as usageCount " +
            "FROM ProductCombo pc " +
            "GROUP BY pc.product " +
            "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedProductsInCombos();
}