package com.tournamentmanager.application.model;

import java.util.List;

public record DatasetSummary(
        String sourceKey,
        String displayName,
        String status,
        long rowCount,
        long distinctCategories,
        Double average,
        Double sum,
        List<CategoryCount> topCategories,
        String message) {

    public static final String OK = "OK";
    public static final String UNAVAILABLE = "UNAVAILABLE";
    public static final String ANALYSIS_FAILED = "ANALYSIS_FAILED";

    public static DatasetSummary unavailable(DatasetDefinition definition, String message) {
        return new DatasetSummary(definition.key(), definition.displayName(), UNAVAILABLE,
                0, 0, null, null, List.of(), message);
    }

    public static DatasetSummary analysisFailed(DatasetDefinition definition, String message) {
        return new DatasetSummary(definition.key(), definition.displayName(), ANALYSIS_FAILED,
                0, 0, null, null, List.of(), message);
    }
}
