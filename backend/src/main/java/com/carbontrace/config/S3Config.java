package com.carbontrace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 client and presigner beans (COMMANDO.md Section 11).
 *
 * <p>Credentials come from the default AWS credential chain (env vars
 * AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY) — never hardcoded.
 */
@Configuration
public class S3Config {

    @Value("${app.aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().region(Region.of(region)).build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder().region(Region.of(region)).build();
    }
}
