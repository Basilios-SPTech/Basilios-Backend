package com.basilios.basilios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "endereco")
@SQLDelete(sql = "UPDATE endereco SET deleted_at = NOW() WHERE id_endereco = ?")
@Where(clause = "deleted_at IS NULL")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long idEndereco;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String rua;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false)
    private String numero;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String bairro;

    @NotBlank
    @Size(max = 8)
    @Column(nullable = false)
    private String cep;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String cidade;

    @NotBlank
    @Size(max = 2)
    @Column(nullable = false)
    private String estado;

    @Size(max = 100)
    @Column
    private String complemento;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    // Construtores
    public Endereco() {}

    public Endereco(String rua, String numero, String bairro, String cep, String cidade, String estado) {
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.estado = estado;
    }

    // Getters e Setters
    public Long getIdEndereco() { return idEndereco; }

    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public void setIdEndereco(Long idEndereco) { this.idEndereco = idEndereco; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    // Métodos utilitários
    public boolean isAtivo() {
        return deletedAt == null;
    }

    public void restaurar() {
        this.deletedAt = null;
    }

    // Métodos sobrescritos
    @Override
    public String toString() {
        return "Endereco{" +
                "idEndereco=" + idEndereco +
                ", rua='" + rua + '\'' +
                ", numero='" + numero + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cep='" + cep + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", complemento='" + complemento + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // se apontam pro mesmo objeto
        if (!(o instanceof Endereco)) return false; // se não for Endereco, já é falso
        Endereco e = (Endereco) o;
        return idEndereco != null && idEndereco.equals(e.idEndereco);
    }

    @Override
    public int hashCode() {
        return idEndereco != null ? idEndereco.hashCode() : 0;
    }
}
