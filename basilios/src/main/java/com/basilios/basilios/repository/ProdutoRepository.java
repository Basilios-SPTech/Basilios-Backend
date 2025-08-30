package com.basilios.basilios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.basilios.basilios.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Integer>{
}
