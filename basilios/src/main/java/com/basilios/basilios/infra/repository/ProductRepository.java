package com.basilios.basilios.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.basilios.basilios.core.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsPausedFalse();

    Page<Product> findByIsPausedFalse(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

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
}