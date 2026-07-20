package com.carbontrace.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * FastAPI and marketplace base URLs (COMMANDO.md Section 11).
 *
 * <p>{@code marketplacePublicBaseUrl} is what Spring Boot passes to the agent so
 * FastAPI knows where the marketplace lives.
 */
@Configuration
@Getter
public class AiConfig {

    @Value("${app.fastapi.base-url}")
    private String fastapiBaseUrl;

    @Value("${app.marketplace.public-base-url}")
    private String marketplacePublicBaseUrl;
}
