package com.basilios.basilios.app.dto.user;

import com.basilios.basilios.core.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProfileResponse {
    private Long id;
    private String nomeUsuario;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private Set<RoleEnum> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
}