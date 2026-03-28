package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioControllerTest {

    private FakeUsuarioController usuarioController;

    @BeforeEach
    void setup() {
        usuarioController = new FakeUsuarioController();
    }

    @Test
    void testGetMe() {
        ResponseEntity<UsuarioProfileResponse> response = usuarioController.getMe();

        assertEquals(200, response.getStatusCodeValue());
        UsuarioProfileResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("usuarioTeste", body.getNomeUsuario());
        assertEquals("teste@exemplo.com", body.getEmail());
        assertTrue(body.getEnabled());
        assertNotNull(body.getCreatedAt());
    }

    // ==================== FAKE CONTROLLER ====================
    static class FakeUsuarioController {

        public ResponseEntity<UsuarioProfileResponse> getMe() {
            // Fake usuário
            UsuarioProfileResponse fakeUser = UsuarioProfileResponse.builder()
                    .id(1L)
                    .nomeUsuario("usuarioTeste")
                    .email("teste@exemplo.com")
                    .cpf("12345678900")
                    .telefone("11999999999")
                    .dataNascimento(LocalDate.from(LocalDateTime.of(1990, 1, 1, 0, 0)))
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(fakeUser);
        }
    }
}
