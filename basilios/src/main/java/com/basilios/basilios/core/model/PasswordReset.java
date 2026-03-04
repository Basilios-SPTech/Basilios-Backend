package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import jakarta.persistence.Id;
import java.time.LocalDateTime;


@Entity
@Table(name = "password_reset")
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
    private Usuario usuario;

    // 🔹 construtor vazio (JPA precisa)
    public PasswordReset() {
    }

    // 🔹 construtor útil (opcional)
    public PasswordReset(String codigo, LocalDateTime expiracao, Usuario usuario) {
        this.codigo = codigo;
        this.expiracao = expiracao;
        this.usuario = usuario;
    }

    // getters e setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getExpiracao() {
        return expiracao;
    }

    public void setExpiracao(LocalDateTime expiracao) {
        this.expiracao = expiracao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}