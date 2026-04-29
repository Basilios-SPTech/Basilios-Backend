package com.basilios.basilios.app.dto.reset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetSenhaDTO {

    @NotBlank(message = "Código é obrigatório")
    private String codigo;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(max = 128, message = "Nova senha deve ter no máximo 128 caracteres")
    private String novaSenha;
}
