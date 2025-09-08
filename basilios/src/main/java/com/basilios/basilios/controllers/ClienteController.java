package com.basilios.basilios.controllers;

import com.basilios.basilios.model.Cliente;
import com.basilios.basilios.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // GET all
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return clienteService.listarClientes();
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    // POST - login
    @PostMapping("/login")
    public ResponseEntity<?> verificarCliente(@RequestBody Map<String, String> credenciais) {
        return clienteService.verificarCliente(credenciais);
    }

    // POST - create
    @PostMapping
    public ResponseEntity<?> inserirCliente(@RequestBody Cliente cliente) {
        return clienteService.inserirCliente(cliente);
    }

    // PUT - update total
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.atualizarCliente(id, cliente);
    }

    // PATCH - update parcial
    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        return clienteService.atualizarParcial(id, campos);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id) {
        return clienteService.deletarCliente(id);
    }
}
