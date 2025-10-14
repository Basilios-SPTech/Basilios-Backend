package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.service.EnderecoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    // 🔹 GET /enderecos → lista todos
    @GetMapping
    public ResponseEntity<List<Address>> listarEnderecos() {
        return enderecoService.listarEnderecos();
    }

    // 🔹 GET /enderecos/{id} → busca por id
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
        return enderecoService.buscarPorId(id);
    }

    // 🔹 POST /enderecos → cria novo
    @PostMapping
    public ResponseEntity<Object> inserirEndereco(@RequestBody Address address) {
        return enderecoService.inserirEndereco(address);
    }

    // 🔹 PUT /enderecos/{id} → atualiza todo o recurso
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarEndereco(@PathVariable Long id, @RequestBody Address address) {
        return enderecoService.atualizarEndereco(id, address);
    }

    // 🔹 PATCH /enderecos/{id} → atualização parcial
    @PatchMapping("/{id}")
    public ResponseEntity<Object> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        return enderecoService.atualizarParcial(id, campos);
    }

    // 🔹 DELETE /enderecos/{id} → remove por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEndereco(@PathVariable Long id) {
        return enderecoService.deletarEndereco(id);
    }
}
