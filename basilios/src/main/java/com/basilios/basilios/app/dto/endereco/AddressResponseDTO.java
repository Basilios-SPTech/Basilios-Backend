package com.basilios.basilios.app.dto.endereco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private Long id;

    private String rua;

    private String numero;

    private String bairro;

    private String cep; // Formatado: 12345-678

    private String cidade;

    private String estado;

    private String complemento;

    private Double latitude;

    private Double longitude;

    private String enderecoCompleto; // Gerado automaticamente

    private Boolean isPrincipal; // Se é o endereço principal do usuário

    private LocalDateTime createdAt;
}