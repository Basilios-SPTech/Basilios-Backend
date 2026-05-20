package com.basilios.basilios.app.dto.upload;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponseDTO {
    private final String url;
    private final String key;
    private final String contentType;
    private final long size;
}

