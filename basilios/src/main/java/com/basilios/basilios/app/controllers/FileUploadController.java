package com.basilios.basilios.app.controllers;

import com.basilios.basilios.infra.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload de arquivos (imagens de produtos)")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @Operation(summary = "Upload de imagem de produto")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = fileStorageService.storeFile(file);
        String fileUrl = "http://localhost:8080/uploads/" + fileName;
        return ResponseEntity.ok(fileUrl);
    }
}
