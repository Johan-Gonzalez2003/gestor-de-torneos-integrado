package com.tournamentmanager.infrastructure.config;

import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.port.out.AnalyticsPort;
import com.tournamentmanager.application.port.out.KnowledgeBasePort;
import com.tournamentmanager.application.port.out.LanguageModelPort;
import com.tournamentmanager.application.port.out.OpenDataSourcePort;
import com.tournamentmanager.application.port.out.TournamentRepositoryPort;
import com.tournamentmanager.application.service.AssistantService;
import com.tournamentmanager.application.service.OpenDataService;
import com.tournamentmanager.application.service.TournamentService;
import com.tournamentmanager.infrastructure.opendata.ResilientOpenDataSource;
import com.tournamentmanager.infrastructure.opendata.SocrataOpenDataSource;
import com.tournamentmanager.infrastructure.resilience.CircuitBreaker;
import com.tournamentmanager.infrastructure.resilience.RetryPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Composición de la aplicación: aquí se cablean puertos con adaptadores.
 * Todas las dependencias se pasan por constructor, así que cada servicio es fácil de probar con mocks.
 */
@Configuration
@EnableConfigurationProperties(OpenDataProperties.class)
public class AppConfig {

    private static final int CIRCUIT_FAILURE_THRESHOLD = 3;
    private static final Duration CIRCUIT_OPEN_DURATION = Duration.ofSeconds(30);
    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final Duration RETRY_INITIAL_BACKOFF = Duration.ofMillis(500);
    private static final double RETRY_MULTIPLIER = 2.0;
    private static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(4);

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public TournamentService tournamentService(TournamentRepositoryPort repository, Clock clock) {
        return new TournamentService(repository, clock);
    }

    @Bean
    public AssistantService assistantService(LanguageModelPort languageModel, KnowledgeBasePort knowledgeBase) {
        return new AssistantService(languageModel, knowledgeBase);
    }

    @Bean
    public OpenDataService openDataService(OpenDataProperties properties, AnalyticsPort analytics, Clock clock) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        List<OpenDataSourcePort> sources = new ArrayList<>();
        for (OpenDataProperties.Source source : properties.sources()) {
            DatasetDefinition definition = new DatasetDefinition(
                    source.key(), source.displayName(), source.numericField(), source.categoryField());
            OpenDataSourcePort raw = new SocrataOpenDataSource(
                    definition, source.baseUrl(), source.datasetId(), httpClient, Duration.ofSeconds(10));
            CircuitBreaker breaker = new CircuitBreaker(
                    source.key(), CIRCUIT_FAILURE_THRESHOLD, CIRCUIT_OPEN_DURATION, clock);
            RetryPolicy retry = RetryPolicy.withThreadSleep(
                    RETRY_MAX_ATTEMPTS, RETRY_INITIAL_BACKOFF, RETRY_MULTIPLIER, RETRY_MAX_BACKOFF);
            sources.add(new ResilientOpenDataSource(raw, breaker, retry));
        }
        return new OpenDataService(sources, analytics);
    }
}
