package com.basilios.basilios.infra.storage;

import com.basilios.basilios.app.dto.upload.FileUploadResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.infra.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("lab-bucket");
        s3Properties.setRegion("us-east-1");
        s3Properties.setPrefix("produtos");

        fileStorageService = new FileStorageService(s3Client, s3Properties);
    }

    @Test
    void storeFile_shouldUploadAndReturnMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "abc".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("ok").build());

        FileUploadResponseDTO response = fileStorageService.storeFile(file);

        assertNotNull(response);
        assertEquals("image/png", response.getContentType());
        assertEquals(3, response.getSize());
        assertTrue(response.getKey().startsWith("produtos/"));
        assertTrue(response.getUrl().startsWith("https://lab-bucket.s3.us-east-1.amazonaws.com/produtos/"));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest sentRequest = requestCaptor.getValue();
        assertEquals("lab-bucket", sentRequest.bucket());
        assertEquals("image/png", sentRequest.contentType());
    }

    @Test
    void storeFile_shouldRejectUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.png",
                "application/pdf",
                "abc".getBytes()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> fileStorageService.storeFile(file));
        assertTrue(ex.getMessage().contains("Tipo de arquivo"));
    }

    @Test
    void storeFile_shouldRejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.bmp",
                "image/png",
                "abc".getBytes()
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> fileStorageService.storeFile(file));
        assertTrue(ex.getMessage().contains("Extensao"));
    }

    @Test
    void storeFile_shouldRejectFileLargerThan5Mb() {
        byte[] largeContent = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                largeContent
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> fileStorageService.storeFile(file));
        assertTrue(ex.getMessage().contains("muito grande"));
    }
}

