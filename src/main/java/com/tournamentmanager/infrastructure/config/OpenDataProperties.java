package com.tournamentmanager.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.opendata")
public record OpenDataProperties(List<Source> sources) {

    public record Source(String key, String displayName, String baseUrl,
                         String datasetId, String numericField, String categoryField) {
    }
}
