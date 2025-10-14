package com.basilios.basilios.core.model;

import com.basilios.basilios.core.enums.CargoEnum;
import com.basilios.basilios.core.enums.TurnoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "funcionario")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Employee extends Usuario {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CargoEnum cargo;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @DecimalMin(value = "0.00", message = "Salário deve ser maior ou igual a zero")
    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno")
    private TurnoEnum turno;

    // Métodos utilitários específicos de Funcionario
    public boolean isDono() {
        return cargo == CargoEnum.DONO;
    }

    public boolean isGerente() {
        return cargo == CargoEnum.GERENTE;
    }

    public boolean isCozinheiro() {
        return cargo == CargoEnum.COZINHEIRO;
    }

    public boolean isEntregador() {
        return cargo == CargoEnum.ENTREGADOR;
    }

    public long getTempoTrabalho() {
        if (dataAdmissao == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dataAdmissao, LocalDate.now());
    }
}