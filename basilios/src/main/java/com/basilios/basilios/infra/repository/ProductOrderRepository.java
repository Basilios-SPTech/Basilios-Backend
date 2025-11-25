package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    /**
     * Busca todos os produtos de um pedido
     */
    List<ProductOrder> findByOrder(Order order);

    /**
     * Busca todos os produtos de um pedido por ID
     */
    List<ProductOrder> findByOrderId(Long orderId);

    /**
     * Busca todos os pedidos que contêm um produto
     */
    List<ProductOrder> findByProduct(Product product);

    /**
     * Busca todos os pedidos que contêm um produto por ID
     */
    List<ProductOrder> findByProductId(Long productId);

    /**
     * Verifica se um pedido possui um produto específico
     */
    boolean existsByOrderAndProduct(Order order, Product product);

    /**
     * Conta quantos produtos um pedido tem
     */
    long countByOrder(Order order);

    /**
     * Conta quantos produtos um pedido tem por ID
     */
    long countByOrderId(Long orderId);

    /**
     * Conta quantas vezes um produto foi pedido
     */
    long countByProduct(Product product);

    /**
     * Conta quantas vezes um produto foi pedido por ID
     */
    long countByProductId(Long productId);

    /**
     * Deleta todos os produtos de um pedido
     */
    void deleteByOrder(Order order);

    /**
     * Deleta todos os produtos de um pedido por ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * Busca produtos de um pedido ordenados por nome
     */
    @Query("SELECT po FROM ProductOrder po " +
            "WHERE po.order.id = :orderId " +
            "ORDER BY po.productName ASC")
    List<ProductOrder> findByOrderIdOrderedByProductName(@Param("orderId") Long orderId);

    /**
     * Calcula subtotal de um pedido
     */
    @Query("SELECT SUM(po.subtotal) FROM ProductOrder po WHERE po.order.id = :orderId")
    BigDecimal calculateOrderSubtotal(@Param("orderId") Long orderId);

    /**
     * Calcula quantidade total de items em um pedido
     */
    @Query("SELECT SUM(po.quantity) FROM ProductOrder po WHERE po.order.id = :orderId")
    Integer getTotalItemsByOrderId(@Param("orderId") Long orderId);

    /**
     * Busca items com promoção em um pedido
     */
    @Query("SELECT po FROM ProductOrder po " +
            "WHERE po.order.id = :orderId AND po.hadPromotion = true")
    List<ProductOrder> findPromotionalItemsByOrderId(@Param("orderId") Long orderId);

    /**
     * Calcula desconto total de promoções em um pedido
     */
    @Query("SELECT SUM(po.originalPrice - po.unitPrice) * po.quantity " +
            "FROM ProductOrder po " +
            "WHERE po.order.id = :orderId AND po.hadPromotion = true")
    BigDecimal calculateTotalPromotionDiscount(@Param("orderId") Long orderId);

    /**
     * Busca produtos mais vendidos (ranking)
     */
    @Query("SELECT po.product, SUM(po.quantity) as totalSold " +
            "FROM ProductOrder po " +
            "GROUP BY po.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();

    /**
     * Busca produtos mais vendidos em um período
     */
    @Query("SELECT po.product, SUM(po.quantity) as totalSold " +
            "FROM ProductOrder po " +
            "WHERE po.order.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY po.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProductsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calcula receita total de um produto
     */
    @Query("SELECT SUM(po.subtotal) FROM ProductOrder po WHERE po.product.id = :productId")
    BigDecimal calculateProductRevenue(@Param("productId") Long productId);

    /**
     * Calcula receita total de um produto em um período
     */
    @Query("SELECT SUM(po.subtotal) FROM ProductOrder po " +
            "WHERE po.product.id = :productId " +
            "AND po.order.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateProductRevenueByPeriod(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Busca produtos nunca vendidos
     */
    @Query("SELECT p FROM Product p " +
            "WHERE NOT EXISTS (SELECT po FROM ProductOrder po WHERE po.product = p)")
    List<Product> findNeverSoldProducts();

    /**
     * Busca produtos com observações (personalizações)
     */
    @Query("SELECT po FROM ProductOrder po " +
            "WHERE po.order.id = :orderId AND po.observations IS NOT NULL")
    List<ProductOrder> findItemsWithObservations(@Param("orderId") Long orderId);

    /**
     * Estatísticas gerais de vendas
     */
    @Query("SELECT " +
            "COUNT(po), " +
            "SUM(po.quantity), " +
            "SUM(po.subtotal), " +
            "AVG(po.unitPrice), " +
            "COUNT(DISTINCT po.product) " +
            "FROM ProductOrder po")
    Object[] getSalesStatistics();

    /**
     * Estatísticas de vendas por período
     */
    @Query("SELECT " +
            "COUNT(po), " +
            "SUM(po.quantity), " +
            "SUM(po.subtotal), " +
            "AVG(po.unitPrice) " +
            "FROM ProductOrder po " +
            "WHERE po.order.createdAt BETWEEN :startDate AND :endDate")
    Object[] getSalesStatisticsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ========== NOVO: verifica se um produto teve itens em promoção no período ==========
    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM ProductOrder po " +
            "WHERE po.product.id = :productId AND po.hadPromotion = true " +
            "AND po.order.createdAt BETWEEN :startDate AND :endDate")
    boolean existsPromotionForProductInPeriod(@Param("productId") Long productId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}