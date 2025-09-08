package com.basilios.basilios.controllers;

import com.basilios.basilios.model.Endereco;
import com.basilios.basilios.service.EnderecoService;
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
    public ResponseEntity<List<Endereco>> listarEnderecos() {
        return enderecoService.listarEnderecos();
    }

    // 🔹 GET /enderecos/{id} → busca por id
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
        return enderecoService.buscarPorId(id);
    }

    // 🔹 POST /enderecos → cria novo
    @PostMapping
    public ResponseEntity<Object> inserirEndereco(@RequestBody Endereco endereco) {
        return enderecoService.inserirEndereco(endereco);
    }

    // 🔹 PUT /enderecos/{id} → atualiza todo o recurso
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarEndereco(@PathVariable Long id, @RequestBody Endereco endereco) {
        return enderecoService.atualizarEndereco(id, endereco);
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
