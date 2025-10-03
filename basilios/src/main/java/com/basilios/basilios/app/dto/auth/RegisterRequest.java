package com.basilios.basilios.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // Dados básicos de autenticação
    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
    private String nomeUsuario;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

    // Dados pessoais
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
            message = "CPF inválido. Use o formato: 12345678901 ou 123.456.789-01")
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$|^\\d{10,11}$",
            message = "Telefone inválido. Use o formato: (11) 99999-9999 ou 11999999999")
    private String telefone;

    private LocalDate dataNascimento;

    // Endereço (obrigatório no cadastro)
    @NotBlank(message = "Rua é obrigatória")
    @Size(max = 255, message = "Rua deve ter no máximo 255 caracteres")
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numero;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
    private String complemento;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Use o formato: 12345-678 ou 12345678")
    private String cep;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @Pattern(regexp = "[A-Z]{2}", message = "Estado deve ter 2 letras maiúsculas (ex: SP)")
    private String estado;

    // Coordenadas do endereço (opcional)
    private Double latitude;
    private Double longitude;
}