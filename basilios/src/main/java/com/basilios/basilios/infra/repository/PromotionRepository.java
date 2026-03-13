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
}