package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca cliente por email
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca cliente por nomeUsuario
     */
    Optional<Cliente> findByNomeUsuario(String nomeUsuario);

    /**
     * Busca cliente por CPF
     */
    Optional<Cliente> findByCpf(String cpf);

    /**
     * Busca clientes ativos (não deletados)
     */
    @Query("SELECT c FROM Cliente c WHERE c.deletedAt IS NULL")
    List<Cliente> findAllAtivos();

    /**
     * Busca cliente com seus pedidos
     */
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.pedidos WHERE c.id = :id")
    Optional<Cliente> findByIdWithPedidos(@Param("id") Long id);

    /**
     * Conta total de clientes ativos
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.deletedAt IS NULL")
    long countAtivos();
}