package com.basilios.basilios.infra.repository;

import com.basilios.basilios.core.enums.CargoEnum;
import com.basilios.basilios.core.enums.TurnoEnum;
import com.basilios.basilios.core.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Busca funcionário por email
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Busca funcionário por nome de usuário
     */
    Optional<Employee> findByNomeUsuario(String nomeUsuario);

    /**
     * Busca funcionários por cargo
     */
    List<Employee> findByCargo(CargoEnum cargo);

    /**
     * Busca funcionários por turno
     */
    List<Employee> findByTurno(TurnoEnum turno);

    /**
     * Busca todos os funcionários ativos (não deletados)
     */
    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    List<Employee> findAllActive();

    /**
     * Busca funcionários ativos por cargo
     */
    @Query("SELECT e FROM Employee e WHERE e.cargo = :cargo AND e.deletedAt IS NULL")
    List<Employee> findByCargoActive(@Param("cargo") CargoEnum cargo);

    /**
     * Busca funcionários ativos por turno
     */
    @Query("SELECT e FROM Employee e WHERE e.turno = :turno AND e.deletedAt IS NULL")
    List<Employee> findByTurnoActive(@Param("turno") TurnoEnum turno);

    /**
     * Conta funcionários ativos
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deletedAt IS NULL")
    long countActive();

    /**
     * Conta funcionários por cargo
     */
    long countByCargo(CargoEnum cargo);
}