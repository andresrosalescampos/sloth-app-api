package com.slothlife.chat.api.infrastructure.adapter.out.gemini;

import com.slothlife.chat.api.domain.port.out.GeminiPort;
import com.slothlife.chat.api.infrastructure.adapter.out.gemini.dto.GeminiRequest;
import com.slothlife.chat.api.infrastructure.adapter.out.gemini.dto.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GeminiAdapter implements GeminiPort {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAdapter(
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generateResponse(String message) {
        log.info("Sending message to Gemini model: {}", model);
        GeminiResponse response = restClient.post()
                .uri("/{model}:generateContent?key={key}", model, apiKey)
                .body(GeminiRequest.of(message))
                .retrieve()
                .body(GeminiResponse.class);
        return response != null ? response.extractText() : "";
    }
}
