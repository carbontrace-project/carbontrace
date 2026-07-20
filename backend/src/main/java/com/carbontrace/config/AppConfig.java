package com.carbontrace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Provides the RestTemplate used to call the FastAPI service.
 *
 * <p>Timeouts cover the slowest FastAPI call in COMMANDO.md Section 18:
 * /agent/purchase (connect 5s, read 120s). /extract (read 90s) and /calculate
 * (read 15s) fit comfortably inside the same read budget.
 */
@Configuration
public class AppConfig {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 120_000;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }
}
