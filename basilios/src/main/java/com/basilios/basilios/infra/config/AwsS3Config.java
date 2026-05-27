package com.basilios.basilios.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class AwsS3Config {

    @Bean
    public S3Client s3Client(S3Properties s3Properties, Environment environment) {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(resolveCredentialsProvider(environment))
                .build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider(Environment environment) {
        String accessKeyId = environment.getProperty("aws.access-key-id");
        String secretAccessKey = environment.getProperty("aws.secret-access-key");
        String sessionToken = environment.getProperty("aws.session-token");

        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey)) {
            return DefaultCredentialsProvider.create();
        }

        if (StringUtils.hasText(sessionToken)) {
            return StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
            );
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }
}
