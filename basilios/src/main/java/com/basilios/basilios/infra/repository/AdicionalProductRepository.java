package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.AdicionalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdicionalProductRepository extends JpaRepository<AdicionalProduct, Long> {

    List<AdicionalProduct> findByProductId(Long productId);

    boolean existsByProductIdAndAdicionalId(Long productId, Long adicionalId);
}
