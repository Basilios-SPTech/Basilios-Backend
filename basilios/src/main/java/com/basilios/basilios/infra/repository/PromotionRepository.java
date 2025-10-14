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

    /**
     * Busca promoções ativas
     */
    List<Promotion> findByIsActiveTrue();

    /**
     * Busca promoções inativas
     */
    List<Promotion> findByIsActiveFalse();

    /**
     * Busca promoções por título (ignora case)
     */
    List<Promotion> findByTitleContainingIgnoreCase(String title);

    /**
     * Busca promoções vigentes (ativas e dentro do período)
     */
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findCurrentPromotions(@Param("today") LocalDate today);

    /**
     * Busca promoções futuras (agendadas)
     */
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.startDate > :today")
    List<Promotion> findScheduledPromotions(@Param("today") LocalDate today);

    /**
     * Busca promoções expiradas
     */
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :today")
    List<Promotion> findExpiredPromotions(@Param("today") LocalDate today);

    /**
     * Busca promoções de um produto específico
     */
    @Query("SELECT p FROM Promotion p JOIN p.products prod WHERE prod.id = :productId")
    List<Promotion> findByProductId(@Param("productId") Long productId);

    /**
     * Busca promoções vigentes de um produto específico
     */
    @Query("SELECT p FROM Promotion p JOIN p.products prod " +
            "WHERE prod.id = :productId AND p.isActive = true " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findCurrentPromotionsByProductId(@Param("productId") Long productId,
                                                     @Param("today") LocalDate today);

    /**
     * Conta promoções ativas
     */
    long countByIsActiveTrue();

    /**
     * Conta promoções vigentes
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    long countCurrentPromotions(@Param("today") LocalDate today);
}