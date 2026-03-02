package com.basilios.basilios.app.dto.user;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UsuarioListarDTO {
    private Long id;
    private String nomeUsuario;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
}
