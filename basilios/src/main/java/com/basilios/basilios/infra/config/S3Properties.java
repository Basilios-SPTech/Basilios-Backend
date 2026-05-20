package com.basilios.basilios.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    private String bucket;
    private String region = "us-east-1";
    private String prefix = "produtos";
    private String publicBaseUrl;
}

