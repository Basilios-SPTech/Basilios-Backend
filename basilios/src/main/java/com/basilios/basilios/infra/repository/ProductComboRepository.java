package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.ProductCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductComboRepository extends JpaRepository<ProductCombo, Long> {

    long countByProductId(Long productId);

    List<ProductCombo> findByProductId(Long productId);

    @Query("SELECT pc.product, COUNT(pc) as usageCount " +
            "FROM ProductCombo pc " +
            "GROUP BY pc.product " +
            "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedProductsInCombos();
}