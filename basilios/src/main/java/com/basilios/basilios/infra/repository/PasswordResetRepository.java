package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetRepository
        extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByCodigo(String codigo);

    void deleteByUsuarioId(Long usuarioId);

    void deleteByExpiracaoBefore(LocalDateTime now);
}
