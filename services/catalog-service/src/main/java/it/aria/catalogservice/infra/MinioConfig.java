package it.aria.catalogservice.infra;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Bean
  public MinioClient minioClient(
      @Value("${aria.minio.url}") String url,
      @Value("${aria.minio.accessKey}") String accessKey,
      @Value("${aria.minio.secretKey}") String secretKey
  ) {
    return MinioClient.builder()
        .endpoint(url)
        .credentials(accessKey, secretKey)
        .build();
  }
}
