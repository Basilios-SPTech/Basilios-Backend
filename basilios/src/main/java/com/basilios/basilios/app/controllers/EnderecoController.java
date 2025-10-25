//package com.basilios.basilios.app.controllers;
//
//import com.basilios.basilios.core.model.Address;
//import com.basilios.basilios.core.service.AddressService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/enderecos")
//public class EnderecoController {
//
//    private final AddressService addressService;
//
//    public EnderecoController(AddressService addressService) {
//        this.addressService = addressService;
//    }
//
//    // 🔹 GET /enderecos → lista todos
//    @GetMapping
//    public ResponseEntity<List<Address>> listarEnderecos() {
//        return addressService.listarEnderecos();
//    }
//
//    // 🔹 GET /enderecos/{id} → busca por id
//    @GetMapping("/{id}")
//    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
//        return addressService.buscarPorId(id);
//    }
//
//    // 🔹 POST /enderecos → cria novo
//    @PostMapping
//    public ResponseEntity<Object> inserirEndereco(@RequestBody Address address) {
//        return addressService.inserirEndereco(address);
//    }
//
//    // 🔹 PUT /enderecos/{id} → atualiza todo o recurso
//    @PutMapping("/{id}")
//    public ResponseEntity<Object> atualizarEndereco(@PathVariable Long id, @RequestBody Address address) {
//        return addressService.atualizarEndereco(id, address);
//    }
//
//    // 🔹 PATCH /enderecos/{id} → atualização parcial
//    @PatchMapping("/{id}")
//    public ResponseEntity<Object> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
//        return addressService.atualizarParcial(id, campos);
//    }
//
//    // 🔹 DELETE /enderecos/{id} → remove por id
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Object> deletarEndereco(@PathVariable Long id) {
//        return addressService.deletarEndereco(id);
//    }
//}
