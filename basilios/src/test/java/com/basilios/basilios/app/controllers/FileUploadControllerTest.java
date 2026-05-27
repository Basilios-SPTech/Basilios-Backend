package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.upload.FileUploadResponseDTO;
import com.basilios.basilios.infra.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileUploadController fileUploadController;

    @Test
    void uploadImage_shouldReturnUploadedFileMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "abc".getBytes()
        );

        FileUploadResponseDTO expectedResponse = FileUploadResponseDTO.builder()
                .url("https://bucket.s3.us-east-1.amazonaws.com/produtos/test.png")
                .key("produtos/test.png")
                .contentType("image/png")
                .size(3)
                .build();

        when(fileStorageService.storeFile(file)).thenReturn(expectedResponse);

        ResponseEntity<FileUploadResponseDTO> response = fileUploadController.uploadImage(file);

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("produtos/test.png", response.getBody().getKey());
        assertEquals("image/png", response.getBody().getContentType());
        verify(fileStorageService).storeFile(file);
    }
}

