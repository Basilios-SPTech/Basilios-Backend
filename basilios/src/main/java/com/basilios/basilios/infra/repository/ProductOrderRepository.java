package com.basilios.basilios.infra.repository;

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

    long countByProductId(Long productId);

    @Query("SELECT po.product, SUM(po.quantity) as totalSold " +
            "FROM ProductOrder po " +
            "GROUP BY po.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();

    @Query("SELECT po.product, SUM(po.quantity) as totalSold " +
            "FROM ProductOrder po " +
            "WHERE po.order.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY po.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProductsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(po.subtotal) FROM ProductOrder po WHERE po.product.id = :productId")
    BigDecimal calculateProductRevenue(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p " +
            "WHERE NOT EXISTS (SELECT po FROM ProductOrder po WHERE po.product = p)")
    List<Product> findNeverSoldProducts();

    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM ProductOrder po " +
            "WHERE po.product.id = :productId AND po.hadPromotion = true " +
            "AND po.order.createdAt BETWEEN :startDate AND :endDate")
    boolean existsPromotionForProductInPeriod(@Param("productId") Long productId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(po.quantity) FROM ProductOrder po WHERE po.order.status = 'ENTREGUE' AND po.order.createdAt BETWEEN :startDate AND :endDate")
    Long sumQuantityByDeliveredOrdersInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}