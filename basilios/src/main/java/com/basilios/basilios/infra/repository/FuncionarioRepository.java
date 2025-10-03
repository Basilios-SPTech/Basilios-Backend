package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.CargoEnum;
import com.basilios.basilios.core.enums.TurnoEnum;
import com.basilios.basilios.core.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    /**
     * Busca funcionário por email
     */
    Optional<Funcionario> findByEmail(String email);

    /**
     * Busca funcionário por nomeUsuario
     */
    Optional<Funcionario> findByNomeUsuario(String nomeUsuario);

    /**
     * Busca funcionários por cargo
     */
    List<Funcionario> findByCargo(CargoEnum cargo);

    /**
     * Busca funcionários por turno
     */
    List<Funcionario> findByTurno(TurnoEnum turno);

    /**
     * Busca funcionários ativos (não deletados)
     */
    @Query("SELECT f FROM Funcionario f WHERE f.deletedAt IS NULL")
    List<Funcionario> findAllAtivos();

    /**
     * Busca funcionários ativos por cargo
     */
    @Query("SELECT f FROM Funcionario f WHERE f.cargo = :cargo AND f.deletedAt IS NULL")
    List<Funcionario> findByCargoAtivos(@Param("cargo") CargoEnum cargo);

    /**
     * Busca funcionários ativos por turno
     */
    @Query("SELECT f FROM Funcionario f WHERE f.turno = :turno AND f.deletedAt IS NULL")
    List<Funcionario> findByTurnoAtivos(@Param("turno") TurnoEnum turno);

    /**
     * Conta funcionários ativos
     */
    @Query("SELECT COUNT(f) FROM Funcionario f WHERE f.deletedAt IS NULL")
    long countAtivos();

    /**
     * Conta funcionários por cargo
     */
    long countByCargo(CargoEnum cargo);
}