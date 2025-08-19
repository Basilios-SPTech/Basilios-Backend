package com.basilios.basilios.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @GetMapping
    public ResponseEntity<Void> getCliente(){
        return ResponseEntity.status(200).build();
    }
}
