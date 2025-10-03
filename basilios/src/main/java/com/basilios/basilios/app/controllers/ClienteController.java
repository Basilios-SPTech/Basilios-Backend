package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Cliente;
import com.basilios.basilios.core.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Lista todos os clientes (apenas ADMIN)
     * GET /api/clientes
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Cliente>> listarClientes() {
        List<Cliente> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Busca cliente por ID (apenas ADMIN)
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Atualiza cliente por ID (apenas ADMIN)
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cliente> atualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        Cliente atualizado = clienteService.atualizarCliente(id, cliente);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Atualização parcial (apenas ADMIN)
     * PATCH /api/clientes/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cliente> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Cliente atualizado = clienteService.atualizarParcial(id, campos);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Deleta cliente (soft delete) (apenas ADMIN)
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        clienteService.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }
}