package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promocao")
public class Promocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocao")
    private Long idPromocao;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", foreignKey = @ForeignKey(name = "fk_promocao_produto"))
    private Produto produto;

    @DecimalMin(value = "0.00", message = "Novo preço deve ser maior ou igual a zero")
    @Column(name = "novo_preco", precision = 10, scale = 2)
    private BigDecimal novoPreco;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "ativo", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Construtores
    public Promocao() {}

    public Promocao(String titulo, LocalDate dataInicio, LocalDate dataFim) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public Promocao(String titulo, String descricao, Produto produto, BigDecimal novoPreco,
                    LocalDate dataInicio, LocalDate dataFim) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.produto = produto;
        this.novoPreco = novoPreco;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // Getters e Setters
    public Long getIdPromocao() {
        return idPromocao;
    }

    public void setIdPromocao(Long idPromocao) {
        this.idPromocao = idPromocao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal getNovoPreco() {
        return novoPreco;
    }

    public void setNovoPreco(BigDecimal novoPreco) {
        this.novoPreco = novoPreco;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
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

    // Métodos utilitários
    public boolean isVigente() {
        if (!ativo) return false;

        LocalDate hoje = LocalDate.now();
        return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim);
    }

    public boolean isExpirada() {
        return LocalDate.now().isAfter(dataFim);
    }

    public boolean isAgendada() {
        return LocalDate.now().isBefore(dataInicio);
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void alternarStatus() {
        this.ativo = !this.ativo;
    }

    public BigDecimal calcularDesconto() {
        if (produto == null || novoPreco == null) {
            return BigDecimal.ZERO;
        }
        return produto.getPreco().subtract(novoPreco);
    }

    public BigDecimal calcularPercentualDesconto() {
        if (produto == null || novoPreco == null || produto.getPreco().equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        BigDecimal desconto = calcularDesconto();
        return desconto.divide(produto.getPreco(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public boolean isValidaPeriodo() {
        return dataFim != null && dataInicio != null && !dataFim.isBefore(dataInicio);
    }

    // Validação customizada
    @PrePersist
    @PreUpdate
    private void validarDados() {
        if (!isValidaPeriodo()) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        if (produto != null && novoPreco != null && novoPreco.compareTo(produto.getPreco()) >= 0) {
            throw new IllegalArgumentException("Novo preço deve ser menor que o preço original do produto");
        }
    }

    // toString
    @Override
    public String toString() {
        return "Promocao{" +
                "idPromocao=" + idPromocao +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", produto=" + (produto != null ? produto.getIdProduto() : null) +
                ", novoPreco=" + novoPreco +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", ativo=" + ativo +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Promocao promocao = (Promocao) o;

        return idPromocao != null ? idPromocao.equals(promocao.idPromocao) : promocao.idPromocao == null;
    }

    @Override
    public int hashCode() {
        return idPromocao != null ? idPromocao.hashCode() : 0;
    }
}