package com.basilios.basilios.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.basilios.basilios.core.model.Promocao;

public interface PromocaoRepository extends JpaRepository<Promocao, Integer>{
}
