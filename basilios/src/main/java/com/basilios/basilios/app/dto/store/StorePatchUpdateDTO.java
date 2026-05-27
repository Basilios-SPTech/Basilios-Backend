package com.basilios.basilios.app.dto.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorePatchUpdateDTO {

    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String name;

    @Size(max = 500, message = "O endereco deve ter no maximo 500 caracteres")
    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude invalida")
    @DecimalMax(value = "90.0", message = "Latitude invalida")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalida")
    @DecimalMax(value = "180.0", message = "Longitude invalida")
    private Double longitude;

    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
    private String phone;

    @DecimalMin(value = "0.00", message = "A taxa de entrega nao pode ser negativa")
    private BigDecimal deliveryFee;
}
