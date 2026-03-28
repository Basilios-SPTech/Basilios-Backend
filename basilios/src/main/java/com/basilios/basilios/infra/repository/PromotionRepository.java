package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findCurrentPromotions(@Param("today") LocalDate today);

    @Query("SELECT p FROM Promotion p JOIN p.products prod " +
            "WHERE prod.id = :productId AND p.isActive = true " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findCurrentPromotionsByProductId(@Param("productId") Long productId,
                                                     @Param("today") LocalDate today);

    /**
     * Query otimizada usando Native SQL para evitar problemas com lazy loading
     */
    @Query(value = "SELECT p.id, p.title, p.description, p.discount_percentage, p.discount_amount, " +
                   "p.start_date, p.end_date, p.is_active, p.created_at, p.updated_at, " +
                   "(SELECT MIN(prod.id) FROM promotion_product pp JOIN product prod ON pp.product_id = prod.id WHERE pp.promotion_id = p.id) as productId " +
                   "FROM promotion p " +
                   "WHERE p.is_active = 1 AND p.start_date <= :today AND p.end_date >= :today",
           nativeQuery = true)
    List<Object[]> findCurrentPromotionsWithProductIds(@Param("today") LocalDate today);
}