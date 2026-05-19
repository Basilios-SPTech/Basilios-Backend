package com.basilios.basilios.infra.storage;

import com.basilios.basilios.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public FileStorageService(
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.region}") String region) {
        this.bucket = bucket;
        this.region = region;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Faz upload do arquivo para o S3 e retorna a URL pública.
     */
    public String storeFile(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Tipo de arquivo não permitido. Tipos aceitos: JPEG, PNG, WebP, GIF");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Extensão de arquivo não permitida. Extensões aceitas: .jpg, .jpeg, .png, .webp, .gif");
        }

        String key = "produtos/" + UUID.randomUUID() + extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
