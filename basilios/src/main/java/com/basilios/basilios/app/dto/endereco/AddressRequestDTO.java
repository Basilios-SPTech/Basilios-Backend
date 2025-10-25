package com.basilios.basilios.app.dto.endereco;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDTO {

    @NotBlank(message = "Rua é obrigatória")
    @Size(max = 255, message = "Rua deve ter no máximo 255 caracteres")
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numero;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Formato: 12345-678 ou 12345678")
    private String cep;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Pattern(regexp = "[A-Z]{2}", message = "Estado deve ter 2 letras maiúsculas (ex: SP)")
    @Size(min = 2, max = 2, message = "Estado deve ter exatamente 2 caracteres")
    private String estado;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
    private String complemento;

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin(value = "-90.0", message = "Latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "Latitude deve estar entre -90 e 90")
    private Double latitude;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin(value = "-180.0", message = "Longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "Longitude deve estar entre -180 e 180")
    private Double longitude;
}