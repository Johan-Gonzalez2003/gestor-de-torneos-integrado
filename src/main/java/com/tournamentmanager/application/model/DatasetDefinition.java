package com.tournamentmanager.application.model;

/** Describe una fuente: qué campo numérico y qué campo categórico usar en el análisis. */
public record DatasetDefinition(String key, String displayName, String numericField, String categoryField) {
}
