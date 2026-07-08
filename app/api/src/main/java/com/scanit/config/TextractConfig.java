package com.scanit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class TextractConfig {
    @Value("${spring.cloud.aws.region.static}")
    private String region;
    
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;
    
    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;
    
    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
    }
}
