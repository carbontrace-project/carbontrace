package com.carbontrace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Provides the RestTemplates used to call the FastAPI service.
 *
 * <p>COMMANDO.md Section 18 gives each flow its OWN read timeout, and they
 * differ by an order of magnitude:
 *
 * <table border="1">
 *   <caption>Section 18 per-flow timeouts</caption>
 *   <tr><th>Flow</th><th>Call</th><th>Connect</th><th>Read</th></tr>
 *   <tr><td>4</td><td>/extract</td><td>5s</td><td>90s</td></tr>
 *   <tr><td>5</td><td>/calculate</td><td>5s</td><td>15s</td></tr>
 *   <tr><td>6</td><td>/agent/purchase</td><td>5s</td><td>120s</td></tr>
 * </table>
 *
 * <p>A read timeout is a LIMIT, not a budget, so one 120s template cannot serve
 * all three: it would let a hung service hold a {@code /calculate} request — and
 * the database connection its transaction owns — for eight times the specified
 * ceiling, and would delay the Section 23 502 by the same margin. STEP A021
 * therefore added the dedicated 15s bean; {@code /extract} gets its own at
 * A-SWAP-1 if the default proves too generous for it.
 *
 * <p>The default stays {@link Primary} at the agent's 120s so the two call sites
 * still to be built (A-SWAP-1, STEP A026) inject it without a qualifier.
 */
@Configuration
public class AppConfig {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int AGENT_READ_TIMEOUT_MS = 120_000;
    private static final int CALCULATE_READ_TIMEOUT_MS = 15_000;

    /** Section 18 Flow 6 — the slowest call, an LLM agent loop. */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return build(AGENT_READ_TIMEOUT_MS);
    }

    /** Section 18 Flow 5 — pure deterministic math, sub-second in practice. */
    @Bean
    public RestTemplate calculationRestTemplate() {
        return build(CALCULATE_READ_TIMEOUT_MS);
    }

    private RestTemplate build(int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
