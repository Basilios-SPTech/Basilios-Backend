package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Find client by email
     */
    Optional<Client> findByEmail(String email);

    /**
     * Find client by username
     */
    Optional<Client> findByNomeUsuario(String nomeUsuario);

    /**
     * Find client by CPF
     */
    Optional<Client> findByCpf(String cpf);

    /**
     * Find all active clients (not deleted)
     */
    @Query("SELECT c FROM Client c WHERE c.deletedAt IS NULL")
    List<Client> findAllActive();

    /**
     * Find client with their orders
     */
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<Client> findByIdWithOrders(@Param("id") Long id);

    /**
     * Count all active clients
     */
    @Query("SELECT COUNT(c) FROM Client c WHERE c.deletedAt IS NULL")
    long countActive();
}
