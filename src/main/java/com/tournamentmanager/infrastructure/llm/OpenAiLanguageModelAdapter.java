package com.tournamentmanager.infrastructure.llm;

import com.tournamentmanager.application.exception.LanguageModelException;
import com.tournamentmanager.application.port.out.LanguageModelPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiLanguageModelAdapter implements LanguageModelPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLanguageModelAdapter.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiLanguageModelAdapter(RestClient.Builder builder,
                                      @Value("${app.openai.api-key:}") String apiKey,
                                      @Value("${app.openai.model:gpt-4o}") String model) {
        this.restClient = builder.baseUrl("https://api.openai.com/v1").build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String generate(String systemPrompt, String userQuestion) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .body(Map.of(
                            "model", model,
                            "temperature", 0.2,
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt),
                                    Map.of("role", "user", "content", userQuestion))))
                    .retrieve()
                    .body(Map.class);
            return extractContent(response);
        } catch (RestClientException ex) {
            log.error("Falló la solicitud al proveedor LLM", ex);
            throw new LanguageModelException(
                    "No fue posible consultar el servicio de IA. Verifica la clave, el modelo y la conexión.");
        }
    }

    @SuppressWarnings("unchecked")
    static String extractContent(Map<String, Object> response) {
        if (response == null || !(response.get("choices") instanceof List<?> choices) || choices.isEmpty()) {
            throw new LanguageModelException("Respuesta del proveedor de IA sin contenido.");
        }
        Map<String, Object> first = (Map<String, Object>) choices.get(0);
        Map<String, Object> message = (Map<String, Object>) first.get("message");
        Object content = message == null ? null : message.get("content");
        if (!(content instanceof String text)) {
            throw new LanguageModelException("Respuesta del proveedor de IA sin texto.");
        }
        return text;
    }
}
