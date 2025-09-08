package com.basilios.basilios.service;

import com.basilios.basilios.model.Cliente;
import com.basilios.basilios.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    public ResponseEntity<?> buscarPorId(Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado."));
    }

    public ResponseEntity<?> verificarCliente(Map<String, String> credenciais) {
        String username = credenciais.get("username");
        String password = credenciais.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username e senha obrigatórios.");
        }

        return clienteRepository.findByNomeUsuario(username)
                .filter(cliente -> cliente.getSenha().equals(password)) // ⚠️ depois a gente troca pra BCrypt.matches
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas."));
    }

    public ResponseEntity<?> inserirCliente(Cliente cliente) {
        if (cliente == null) {
            return ResponseEntity.badRequest().body("Cliente não pode ser nulo.");
        }

        if (cliente.getNomeUsuario() == null || cliente.getNomeUsuario().isBlank()) {
            return ResponseEntity.badRequest().body("Nome de usuário é obrigatório.");
        }

        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            return ResponseEntity.badRequest().body("Senha é obrigatória.");
        }

        if (clienteRepository.existsByNomeUsuario(cliente.getNomeUsuario())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já existe.");
        }

        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    public ResponseEntity<?> atualizarCliente(Long id, Cliente cliente) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
        }
        cliente.setIdCliente(id); // garante que está sobrescrevendo o registro certo
        Cliente atualizado = clienteRepository.save(cliente);
        return ResponseEntity.ok(atualizado);
    }

    public ResponseEntity<?> atualizarParcial(Long id, Map<String, Object> campos) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
        }

        Cliente cliente = clienteOpt.get();

        try {
            for (Map.Entry<String, Object> entry : campos.entrySet()) {
                String campo = entry.getKey();
                Object valor = entry.getValue();

                Field field = Cliente.class.getDeclaredField(campo);
                field.setAccessible(true);
                field.set(cliente, valor);
            }
            Cliente atualizado = clienteRepository.save(cliente);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao atualizar campos: " + e.getMessage());
        }
    }

    public ResponseEntity<?> deletarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
        }

        clienteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
