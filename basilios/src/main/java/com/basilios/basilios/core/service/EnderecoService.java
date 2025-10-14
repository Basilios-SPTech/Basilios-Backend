package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.infra.repository.EnderecoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    // 🔹 Listar todos os endereços
    public ResponseEntity<List<Address>> listarEnderecos() {
        return ResponseEntity.ok(enderecoRepository.findAll());
    }

    // 🔹 Buscar por ID
    public ResponseEntity<Object> buscarPorId(Long id) {
        return enderecoRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Endereço não encontrado."));
    }

    // 🔹 Inserir novo endereço
    public ResponseEntity<Object> inserirEndereco(Address address) {
        if (address == null) {
            return ResponseEntity.badRequest().body("Endereço não pode ser nulo.");
        }
        Address salvo = enderecoRepository.save(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // 🔹 Atualizar endereço por ID (PUT)
    public ResponseEntity<Object> atualizarEndereco(Long id, Address address) {
        if (!enderecoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Endereço não encontrado.");
        }
        address.setIdEndereco(id);
        Address atualizado = enderecoRepository.save(address);
        return ResponseEntity.ok(atualizado);
    }

    // 🔹 Atualizar parcialmente (PATCH)
    public ResponseEntity<Object> atualizarParcial(Long id, Map<String, Object> campos) {
        Optional<Address> enderecoOpt = enderecoRepository.findById(id);

        if (enderecoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Endereço não encontrado.");
        }

        Address address = enderecoOpt.get();

        try {
            for (Map.Entry<String, Object> entry : campos.entrySet()) {
                String campo = entry.getKey();
                Object valor = entry.getValue();

                Field field = Address.class.getDeclaredField(campo);
                field.setAccessible(true);
                field.set(address, valor);
            }
            Address atualizado = enderecoRepository.save(address);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao atualizar campos: " + e.getMessage());
        }
    }

    // 🔹 Deletar por ID
    public ResponseEntity<Object> deletarEndereco(Long id) {
        if (!enderecoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Endereço não encontrado.");
        }
        enderecoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
