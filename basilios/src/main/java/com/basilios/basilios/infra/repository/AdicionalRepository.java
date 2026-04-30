package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Adicional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdicionalRepository extends JpaRepository<Adicional, Long> {

    List<Adicional> findByAvailableTrue();

    Page<Adicional> findByDeletedAtIsNull(Pageable pageable);

    List<Adicional> findByDeletedAtIsNull();

    Optional<Adicional> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
}
