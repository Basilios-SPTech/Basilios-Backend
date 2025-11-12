package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Busca todos os endereços ativos de um usuário
     */
    List<Address> findByUsuarioAndDeletedAtIsNull(Usuario usuario);

    /**
     * Busca endereço por ID e usuário (pra garantir propriedade)
     */
    Optional<Address> findByIdAddressAndUsuario(Long idAddress, Usuario usuario);

    /**
     * Busca o endereço principal de um usuário
     */
    @Query("""
        SELECT a 
        FROM Address a 
        WHERE a.usuario = :usuario 
          AND a = a.usuario.addressPrincipal
          AND a.deletedAt IS NULL
    """)
    Optional<Address> findPrincipalByUsuario(Usuario usuario);

    /**
     * Conta endereços ativos do usuário
     */
    long countByUsuarioAndDeletedAtIsNull(Usuario usuario);

    /**
     * Verifica se o usuário tem endereço principal ativo
     */
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END 
        FROM Address a 
        WHERE a.usuario = :usuario 
          AND a = a.usuario.addressPrincipal 
          AND a.deletedAt IS NULL
    """)
    boolean hasPrincipalAddress(Usuario usuario);
}
