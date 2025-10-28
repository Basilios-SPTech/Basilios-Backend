package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "endereco")
@SQLDelete(sql = "UPDATE endereco SET deleted_at = NOW() WHERE id_endereco = ?")
@Where(clause = "deleted_at IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idEndereco")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long idEndereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_endereco_usuario"))
    @ToString.Exclude
    private Usuario usuario;

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

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    // Métodos utilitários
    public boolean isAtivo() {
        return deletedAt == null;
    }

    public void restaurar() {
        this.deletedAt = null;
    }

    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(rua).append(", ").append(numero);
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        sb.append(", ").append(bairro);
        sb.append(", ").append(cidade).append(" - ").append(estado);
        sb.append(", CEP: ").append(cep);
        return sb.toString();
    }

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
                ", deletedAt=" + deletedAt +
                '}';
    }
}