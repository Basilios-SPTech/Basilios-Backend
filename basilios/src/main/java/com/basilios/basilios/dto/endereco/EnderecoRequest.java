package com.basilios.basilios.dto.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnderecoRequest {
    @NotBlank
    @Size(max = 255)
    private String rua;

    @NotBlank
    @Size(max = 10)
    private String numero;

    @NotBlank
    @Size(max = 100)
    private String bairro;

    @NotBlank
    @Size(max = 8)
    private String cep;

    @NotBlank
    @Size(max = 100)
    private String cidade;

    @NotBlank
    @Size(max = 2)
    private String estado;

    @Size(max = 100)
    private String complemento;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}