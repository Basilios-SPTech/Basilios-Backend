package com.basilios.basilios.model;

import com.basilios.basilios.model.Endereco;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(max = 50, message = "Nome de usuário deve ter no máximo 50 caracteres")
    @Column(name = "nome_usuario", nullable = false, unique = true, length = 50)
    private String nomeUsuario;

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 255, message = "Senha deve ter no máximo 255 caracteres")
    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", foreignKey = @ForeignKey(name = "fk_cliente_endereco"))
    private Endereco endereco;

    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter exatamente 11 dígitos")
    @Column(name = "cpf", length = 11)
    private String cpf;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Construtores
    public Cliente() {}

    public Cliente(String nomeUsuario, String senha) {
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
    }

    public Cliente(String nomeUsuario, String senha, Endereco endereco, String cpf) {
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.endereco = endereco;
        this.cpf = cpf;
    }

    // Getters e Setters
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Métodos utilitários
    public boolean isAtivo() {
        return deletedAt == null;
    }

    public void marcarComoExcluido() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restaurar() {
        this.deletedAt = null;
    }

    // toString
    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", nomeUsuario='" + nomeUsuario + '\'' +
                ", endereco=" + (endereco != null ? endereco.getIdEndereco() : null) +
                ", cpf='" + cpf + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cliente cliente = (Cliente) o;

        return idCliente != null ? idCliente.equals(cliente.idCliente) : cliente.idCliente == null;
    }

    @Override
    public int hashCode() {
        return idCliente != null ? idCliente.hashCode() : 0;
    }
}