package com.ntd.csdl.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {
    @Bean("minioClient")
    MinioClient minioClient(
            @Value("${app.minio.endpoint}") String endpoint,
            @Value("${app.minio.access-key}") String accessKey,
            @Value("${app.minio.secret-key}") String secretKey
    ) {
        return buildClient(endpoint, accessKey, secretKey);
    }

    @Bean("minioPresignClient")
    MinioClient minioPresignClient(
            @Value("${app.minio.public-endpoint}") String endpoint,
            @Value("${app.minio.access-key}") String accessKey,
            @Value("${app.minio.secret-key}") String secretKey
    ) {
        return buildClient(endpoint, accessKey, secretKey);
    }

    private MinioClient buildClient(String endpoint, String accessKey, String secretKey) {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
}
