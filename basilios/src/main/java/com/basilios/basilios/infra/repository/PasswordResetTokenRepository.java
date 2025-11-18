package com.basilios.basilios.infra.repository;



import com.basilios.basilios.core.model.PasswordResetToken;
import com.basilios.basilios.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndUsedFalse(Usuario user);

    void deleteByUser(Usuario user);
}
