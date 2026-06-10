package com.basilios.basilios.infra.storage;

import com.basilios.basilios.app.dto.upload.FileUploadResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.infra.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public FileUploadResponseDTO storeFile(MultipartFile file) {
        validateFile(file);
        validateS3Config();

        String contentType = file.getContentType().toLowerCase();
        String extension = extractExtension(file.getOriginalFilename());
        String key = buildObjectKey(extension);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (S3Exception ex) {
            ex.printStackTrace();
            System.out.println("AWS ERROR CODE: " + ex.awsErrorDetails().errorCode());
            System.out.println("AWS ERROR MESSAGE: " + ex.awsErrorDetails().errorMessage());
            throw ex;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new BusinessException("Nao foi possivel ler o arquivo enviado.");
        }

        return FileUploadResponseDTO.builder()
                .url(buildPublicUrl(key))
                .key(key)
                .contentType(contentType)
                .size(file.getSize())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo obrigatorio.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Arquivo muito grande. Tamanho maximo: 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Tipo de arquivo nao permitido. Tipos aceitos: JPEG, PNG, WebP, GIF");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Extensao de arquivo nao permitida. Extensoes aceitas: .jpg, .jpeg, .png, .webp, .gif");
        }
    }

    private void validateS3Config() {
        if (!StringUtils.hasText(s3Properties.getBucket())) {
            throw new BusinessException("Bucket S3 nao configurado. Defina AWS_S3_BUCKET.");
        }
        if (!StringUtils.hasText(s3Properties.getRegion())) {
            throw new BusinessException("Regiao S3 nao configurada. Defina AWS_REGION.");
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
    }

    private String buildObjectKey(String extension) {
        String prefix = s3Properties.getPrefix();
        String safePrefix = StringUtils.hasText(prefix) ? prefix.trim().replaceAll("^/+|/+$", "") : "produtos";
        return safePrefix + "/" + UUID.randomUUID() + extension;
    }

    private String buildPublicUrl(String key) {
        if (StringUtils.hasText(s3Properties.getPublicBaseUrl())) {
            return s3Properties.getPublicBaseUrl().replaceAll("/+$", "") + "/" + key;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", s3Properties.getBucket(), s3Properties.getRegion(), key);
    }
}
