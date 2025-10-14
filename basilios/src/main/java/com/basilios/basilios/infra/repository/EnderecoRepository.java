package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<Address, Long> {
}
