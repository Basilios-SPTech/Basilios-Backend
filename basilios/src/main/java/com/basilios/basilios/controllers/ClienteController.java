package com.basilios.basilios.controllers;

import com.basilios.basilios.model.Cliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClienteController {

    List<Cliente> clientes = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<Cliente>> getClient(){
        return ResponseEntity.status(200).body(clientes);
    }

    @PostMapping("/verify-client")
    public ResponseEntity<Cliente> verifyClient(@RequestBody Map<String, String> credentials){
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if(username == null  || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()){
                return ResponseEntity.status(400).build();
            }

            Cliente clientFound = clientes.stream()
                    .filter(cliente -> username.equals(cliente.getUsername()) &&
                            password.equals(cliente.getPassword()))
                    .findFirst()
                    .orElse(null);

            if(clientFound != null){
                return ResponseEntity.status(200).body(clientFound);
            }else{
                return ResponseEntity.status(401).build();
            }

        }catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> insertClient(@RequestBody Cliente client){
        try{
            if(client == null){
                return ResponseEntity.status(400).build();
            }

            if (client.getUsername() == null || client.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(400).build();
            }

            if (client.getPassword() == null || client.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).build();
            }

            boolean clientAlreadyExists = clientes.stream()
                    .anyMatch(c -> client.getUsername().equals(c.getUsername()));

            if(clientAlreadyExists){
                return ResponseEntity.status(409).build();
            }

            clientes.add(client);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
