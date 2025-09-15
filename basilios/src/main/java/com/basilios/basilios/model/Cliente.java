package com.basilios.basilios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@SQLDelete(sql = "UPDATE cliente SET deleted_at = NOW() WHERE id_cliente = ?")
@Where(clause = "deleted_at IS NULL")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nome_usuario", nullable = false, unique = true)
    private String nomeUsuario;

    @NotBlank
    @Size(max = 255)
    @Column(name = "senha", nullable = false)
    private String senha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", foreignKey = @ForeignKey(name = "fk_cliente_endereco"))
    private Endereco endereco;

@CPF
@NotBlank
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

    public Cliente() {}

    public Cliente(String nomeUsuario, String senha) {
        this.nomeUsuario = nomeUsuario;
        setSenha(senha); // já faz hash
    }

    public Cliente(String nomeUsuario, String senha, Endereco endereco, String cpf) {
        this.nomeUsuario = nomeUsuario;
        setSenha(senha);
        this.endereco = endereco;
        this.cpf = cpf;
    }

    // Getters e Setters
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getSenha() { return senha; }

    public void setSenha(String senha) {
       this.senha = senha;
    }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public boolean isAtivo() { return deletedAt == null; }

    public void restaurar() { this.deletedAt = null; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente cliente = (Cliente) o;
        return idCliente != null && idCliente.equals(cliente.idCliente);
    }

    @Override
    public int hashCode() { return idCliente != null ? idCliente.hashCode() : 0; }
}
