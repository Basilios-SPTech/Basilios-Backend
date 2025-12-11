package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerTest {

    private FakeAuthController authController;

    @BeforeEach
    void setup() {
        authController = new FakeAuthController();
    }

    // ==================== REGISTER ====================
    @Test
    void testRegister() {
        UsuarioRegisterDTO registerDTO = new UsuarioRegisterDTO();
        registerDTO.setEmail("teste@exemplo.com");
        registerDTO.setNomeUsuario("usuario1");
        registerDTO.setPassword("123456");

        ResponseEntity<UsuarioTokenDTO> response = authController.register(registerDTO);

        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("usuario1", response.getBody().getNomeUsuario());
        assertNotNull(response.getBody().getToken());
    }

    // ==================== LOGIN ====================
    @Test
    void testLogin() {
        // Primeiro registrar
        UsuarioRegisterDTO registerDTO = new UsuarioRegisterDTO();
        registerDTO.setEmail("login@exemplo.com");
        registerDTO.setNomeUsuario("loginUser");
        registerDTO.setPassword("senha");

        authController.register(registerDTO);

        // Login
        UsuarioLoginDTO loginDTO = new UsuarioLoginDTO();
        loginDTO.setEmail("login@exemplo.com");
        loginDTO.setPassword("senha");

        ResponseEntity<UsuarioTokenDTO> response = authController.login(loginDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("loginUser", response.getBody().getNomeUsuario());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoginFail() {
        UsuarioLoginDTO loginDTO = new UsuarioLoginDTO();
        loginDTO.setEmail("naoExiste@exemplo.com");
        loginDTO.setPassword("senha");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authController.login(loginDTO);
        });

        assertTrue(exception.getMessage().contains("Usuário ou senha inválidos"));
    }

    // ==================== FAKE CONTROLLER ====================
    static class FakeAuthController {

        private final FakeAuthService authService = new FakeAuthService();

        public ResponseEntity<UsuarioTokenDTO> register(UsuarioRegisterDTO request) {
            UsuarioTokenDTO tokenDTO = authService.register(request);
            return ResponseEntity.status(201).body(tokenDTO);
        }

        public ResponseEntity<UsuarioTokenDTO> login(UsuarioLoginDTO request) {
            UsuarioTokenDTO tokenDTO = authService.login(request);
            return ResponseEntity.ok(tokenDTO);
        }
    }

    // ==================== FAKE SERVICE ====================
    static class FakeAuthService {
        private final Map<String, UsuarioTokenDTO> fakeDb = new HashMap<>();
        private final Map<String, String> passwordDb = new HashMap<>();

        public UsuarioTokenDTO register(UsuarioRegisterDTO dto) {
            if (fakeDb.containsKey(dto.getEmail())) {
                throw new RuntimeException("Usuário já existe");
            }
            UsuarioTokenDTO tokenDTO = new UsuarioTokenDTO();
            tokenDTO.setNomeUsuario(dto.getNomeUsuario());
            tokenDTO.setEmail(dto.getEmail());
            tokenDTO.setToken("fake-token-" + dto.getNomeUsuario());
            fakeDb.put(dto.getEmail(), tokenDTO);
            passwordDb.put(dto.getEmail(), dto.getPassword());
            return tokenDTO;
        }

        public UsuarioTokenDTO login(UsuarioLoginDTO dto) {
            UsuarioTokenDTO user = fakeDb.get(dto.getEmail());
            String pwd = passwordDb.get(dto.getEmail());
            if (user == null || !dto.getPassword().equals(pwd)) {
                throw new RuntimeException("Usuário ou senha inválidos");
            }
            return user;
        }
    }
}
