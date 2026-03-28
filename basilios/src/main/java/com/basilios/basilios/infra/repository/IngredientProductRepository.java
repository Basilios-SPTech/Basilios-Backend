package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Ingredient;
import com.basilios.basilios.core.model.IngredientProduct;
import com.basilios.basilios.core.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientProductRepository extends JpaRepository<IngredientProduct, Long> {

    List<IngredientProduct> findByProduct(Product product);

    Page<IngredientProduct> findByProduct(Product product, Pageable pageable);

    Optional<IngredientProduct> findByProductAndIngredient(Product product, Ingredient ingredient);

    boolean existsByProductAndIngredient(Product product, Ingredient ingredient);
}