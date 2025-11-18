package com.basilios.basilios.app.dto.reset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para solicitar reset de senha
@Data
@NoArgsConstructor
@AllArgsConstructor
class PasswordResetRequestDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
}

// DTO para resetar a senha com o token
@Data
@NoArgsConstructor
@AllArgsConstructor
class PasswordResetDTO {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String newPassword;
}

// DTO de resposta
@Data
@NoArgsConstructor
@AllArgsConstructor
class MessageResponseDTO {
    private String message;
}

// Classes públicas para uso externo
public class PasswordResetDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reset {
        @NotBlank(message = "Token é obrigatório")
        private String token;

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String message;
    }
}
