package com.basilios.basilios.infra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.basilios.basilios.core.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========== Buscas por Status ==========
    List<Product> findByIsPausedFalse();
    List<Product> findByIsPausedTrue();
    Page<Product> findByIsPausedFalse(Pageable pageable);

    // ========== Buscas por Nome ==========
    Optional<Product> findByNameIgnoreCase(String name);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByNameContainingIgnoreCaseAndIsPausedFalse(String name);
    boolean existsByNameIgnoreCase(String name);

    // ========== Buscas por Preço ==========
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByPriceBetweenAndIsPausedFalse(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);
    List<Product> findByPriceLessThanEqualAndIsPausedFalse(BigDecimal maxPrice);
    List<Product> findByPriceGreaterThanEqual(BigDecimal minPrice);
    List<Product> findByPriceGreaterThanEqualAndIsPausedFalse(BigDecimal minPrice);

    // ========== Buscas por Ingredientes ==========
    // NOTA: Para buscar produtos por ingredientes, use o IngredientRepository
    // pois o relacionamento é feito através de IngredientProduct (tabela de junção)

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productIngredients pi " +
            "JOIN pi.ingredient i " +
            "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Product> findByIngredientName(@Param("ingredientName") String ingredientName);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productIngredients pi " +
            "JOIN pi.ingredient i " +
            "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%')) " +
            "AND p.isPaused = false")
    List<Product> findByIngredientNameAndActive(@Param("ingredientName") String ingredientName);

    // ========== Ordenações ==========
    Page<Product> findByIsPausedFalseOrderByCreatedAtDesc(Pageable pageable);
    // Corrigidos: nomes derivados inválidos substituídos por findAllBy...
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findByIsPausedFalseOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
    List<Product> findByIsPausedFalseOrderByPriceDesc();
    List<Product> findAllByOrderByNameAsc();

    // ========== Contadores ==========
    long countByIsPausedFalse();
    long countByIsPausedTrue();
    long countByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ========== Consultas Personalizadas ==========
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:activeOnly = false OR p.isPaused = false)")
    List<Product> findWithFilters(@Param("name") String name,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("activeOnly") boolean activeOnly);

    @Query("SELECT p FROM Product p WHERE p.id != :id AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "p.isPaused = false")
    List<Product> findSimilarProducts(@Param("id") Long id, @Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE " +
            "CASE " +
            "WHEN :category = 'ECONOMIC' THEN p.price <= 15.0 " +
            "WHEN :category = 'MEDIUM' THEN p.price > 15.0 AND p.price <= 30.0 " +
            "WHEN :category = 'PREMIUM' THEN p.price > 30.0 " +
            "ELSE true END " +
            "AND (:activeOnly = false OR p.isPaused = false)")
    List<Product> findByPriceCategory(@Param("category") String category,
                                      @Param("activeOnly") boolean activeOnly);

    @Query("SELECT " +
            "COUNT(p), " +
            "SUM(CASE WHEN p.isPaused = false THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.isPaused = true THEN 1 ELSE 0 END), " +
            "AVG(p.price), " +
            "MIN(p.price), " +
            "MAX(p.price) " +
            "FROM Product p")
    Object[] getMenuStatistics();

    @Query("SELECT p FROM Product p WHERE p.isPaused = false ORDER BY p.createdAt DESC")
    List<Product> findRecentlyAdded(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.isPaused = false")
    List<Product> findAvailableProductsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Product p WHERE p.id != :currentId AND p.isPaused = false " +
            "ORDER BY p.createdAt DESC")
    List<Product> findSuggestions(@Param("currentId") Long currentId, Pageable pageable);

    // ========== ANÁLISES AVANÇADAS (Adicione estes) ==========

    /**
     * Produtos agrupados por categoria (para estatísticas)
     */
    @Query("SELECT p.category, COUNT(p) as total, " +
            "SUM(CASE WHEN p.isPaused = false THEN 1 ELSE 0 END) as active, " +
            "AVG(p.price) as avgPrice " +
            "FROM Product p GROUP BY p.category")
    List<Object[]> getCategoryStatistics();

    /**
     * Produtos com promoção vigente
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.promotions promo " +
            "WHERE promo.isActive = true " +
            "AND promo.startDate <= CURRENT_DATE " +
            "AND promo.endDate >= CURRENT_DATE")
    List<Product> findWithActivePromotions();

    /**
     * Produtos seguros para deletar (sem pedidos e combos)
     */
    @Query("SELECT p FROM Product p " +
            "WHERE NOT EXISTS (SELECT 1 FROM ProductOrder po WHERE po.product = p) " +
            "AND NOT EXISTS (SELECT 1 FROM ProductCombo pc WHERE pc.product = p)")
    List<Product> findDeletableProducts();

    /**
     * ABC Analysis - Produtos por contribuição de receita
     */
    @Query("SELECT p, COALESCE(SUM(po.subtotal), 0) as revenue " +
            "FROM Product p " +
            "LEFT JOIN ProductOrder po ON p.id = po.product.id " +
            "GROUP BY p.id " +
            "ORDER BY revenue DESC")
    List<Object[]> getABCAnalysis();

    /**
     * Produtos ordenados por quantidade de vendas
     */
    @Query("SELECT p, COALESCE(SUM(po.quantity), 0) as totalSold " +
            "FROM Product p " +
            "LEFT JOIN ProductOrder po ON p.id = po.product.id " +
            "GROUP BY p.id " +
            "ORDER BY totalSold DESC")
    List<Object[]> getProductsByTotalSoldQuantity();

    /**
     * Conta quantos produtos NÃO tiveram vendas dentro do período (baseado em ProductOrder.createdAt)
     * Exclui ProductOrder cujo pedido foi CANCELADO.
     */
    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE NOT EXISTS (SELECT po FROM ProductOrder po WHERE po.product = p AND po.createdAt BETWEEN :start AND :end AND po.order.status <> com.basilios.basilios.core.enums.StatusPedidoEnum.CANCELADO)")
    Long countProductsNotSoldBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}