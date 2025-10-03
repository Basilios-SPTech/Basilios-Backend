package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long idProduto;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 255, message = "Nome do produto deve ter no máximo 255 caracteres")
    @Column(name = "nome_produto", nullable = false, length = 255)
    private String nomeProduto;

    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredientes", columnDefinition = "json")
    private List<String> ingredientes;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço deve ser maior ou igual a zero")
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_paused", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPaused = false;

    // Construtores
    public Produto() {}

    public Produto(String nomeProduto, BigDecimal preco) {
        this.nomeProduto = nomeProduto;
        this.preco = preco;
    }

    public Produto(String nomeProduto, String descricao, List<String> ingredientes, BigDecimal preco) {
        this.nomeProduto = nomeProduto;
        this.descricao = descricao;
        this.ingredientes = ingredientes;
        this.preco = preco;
    }

    // Getters e Setters
    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<String> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
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

    public Boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(Boolean isPaused) {
        this.isPaused = isPaused;
    }

    // Métodos utilitários
    public boolean isAtivo() {
        return !isPaused;
    }

    public void pausar() {
        this.isPaused = true;
    }

    public void ativar() {
        this.isPaused = false;
    }

    public void alternarStatus() {
        this.isPaused = !this.isPaused;
    }

    // toString
    @Override
    public String toString() {
        return "Produto{" +
                "idProduto=" + idProduto +
                ", nomeProduto='" + nomeProduto + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ingredientes=" + ingredientes +
                ", preco=" + preco +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isPaused=" + isPaused +
                '}';
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Produto produto = (Produto) o;

        return idProduto != null ? idProduto.equals(produto.idProduto) : produto.idProduto == null;
    }

    @Override
    public int hashCode() {
        return idProduto != null ? idProduto.hashCode() : 0;
    }
}