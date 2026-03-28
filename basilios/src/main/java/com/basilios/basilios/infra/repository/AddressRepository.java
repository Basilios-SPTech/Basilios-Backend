package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Busca todos os endereços ativos de um usuário
     */
    List<Address> findByUsuarioAndDeletedAtIsNull(Usuario usuario);

    Page<Address> findByUsuarioAndDeletedAtIsNull(Usuario usuario, Pageable pageable);

    /**
     * Busca endereço por ID e usuário (apenas ativos)
     */
    @Query("""
        SELECT a
        FROM Address a
        WHERE a.idAddress = :idAddress
          AND a.usuario = :usuario
          AND a.deletedAt IS NULL
    """)
    Optional<Address> findByIdAddressAndUsuario(
            @Param("idAddress") Long idAddress,
            @Param("usuario") Usuario usuario
    );

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
    Optional<Address> findPrincipalByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Conta endereços ativos de um usuário
     */
    long countByUsuarioAndDeletedAtIsNull(Usuario usuario);

}