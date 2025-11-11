package com.basilios.basilios.app.dto.user;

import com.basilios.basilios.core.enums.RoleEnum;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
@Data
public class UsuarioListarDTO {
    private String nomeUsuario;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
}
