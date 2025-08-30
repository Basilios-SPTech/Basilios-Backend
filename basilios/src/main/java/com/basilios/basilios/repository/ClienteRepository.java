package com.basilios.basilios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.basilios.basilios.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
