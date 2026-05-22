package com.basilios.basilios.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime createdAt;
    private List<String> roles;
}