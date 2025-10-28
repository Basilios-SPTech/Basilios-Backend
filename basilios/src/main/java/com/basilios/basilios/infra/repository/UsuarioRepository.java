package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca usuário por email OU nomeUsuario
     * Usado para login flexível
     */
    Optional<Usuario> findByEmailOrNomeUsuario(String email, String nomeUsuario);

    /**
     * Busca usuário por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuário por nomeUsuario
     */
    Optional<Usuario> findByNomeUsuario(String nomeUsuario);

    /**
     * Busca usuário por CPF
     */
    Optional<Usuario> findByCpf(String cpf);

    /**
     * Verifica se existe usuário com email
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se existe usuário com nomeUsuario
     */
    boolean existsByNomeUsuario(String nomeUsuario);

    /**
     * Verifica se existe usuário com CPF
     */
    boolean existsByCpf(String cpf);
}