package com.basilios.basilios.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTokenDTO {
    private Long id;
    private String nomeUsuario;
    private String email;
    private String token;
    private List<String> roles;
}