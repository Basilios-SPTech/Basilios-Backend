package com.basilios.basilios.app.dto.reset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetSenhaDTO {

    @NotBlank(message = "Codigo e obrigatorio")
    private String codigo;

    @NotBlank(message = "Nova senha e obrigatoria")
    @Size(min = 8, max = 72, message = "Nova senha deve ter entre 8 e 72 caracteres")
    private String novaSenha;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
