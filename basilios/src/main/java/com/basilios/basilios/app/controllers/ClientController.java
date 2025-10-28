package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Client;
import com.basilios.basilios.core.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * Lists all clients (ADMIN only)
     * GET /api/clients
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Client>> listClients() {
        List<Client> clients = clientService.listClients();
        return ResponseEntity.ok(clients);
    }

    /**
     * Finds client by ID (ADMIN only)
     * GET /api/clients/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Client> findById(@PathVariable Long id) {
        Client client = clientService.findById(id);
        return ResponseEntity.ok(client);
    }

    /**
     * Updates client by ID (ADMIN only)
     * PUT /api/clients/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        Client updated = clientService.updateClient(id, client);
        return ResponseEntity.ok(updated);
    }

    /**
     * Partial update (ADMIN only)
     * PATCH /api/clients/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Client> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        Client updated = clientService.partialUpdate(id, fields);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes client (soft delete) (ADMIN only)
     * DELETE /api/clients/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
