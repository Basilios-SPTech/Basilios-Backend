package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime expiracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    public PasswordReset(String codigo, LocalDateTime expiracao, Usuario usuario) {
        this.codigo = codigo;
        this.expiracao = expiracao;
        this.usuario = usuario;
    }

    public boolean isExpirado() {
        return expiracao.isBefore(LocalDateTime.now());
    }
}