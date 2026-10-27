package com.tournamentmanager.application.service;

import com.tournamentmanager.application.exception.AnalysisException;
import com.tournamentmanager.application.exception.OpenDataUnavailableException;
import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.model.DatasetSummary;
import com.tournamentmanager.application.port.out.AnalyticsPort;
import com.tournamentmanager.application.port.out.OpenDataSourcePort;

import java.util.ArrayList;
import java.util.List;

/**
 * Consume las fuentes abiertas y devuelve un resumen por fuente.
 * Una fuente caída no tumba las demás: se reporta como UNAVAILABLE.
 */
public class OpenDataService {

    private final List<OpenDataSourcePort> sources;
    private final AnalyticsPort analytics;

    public OpenDataService(List<OpenDataSourcePort> sources, AnalyticsPort analytics) {
        this.sources = List.copyOf(sources);
        this.analytics = analytics;
    }

    public List<DatasetSummary> summarizeAll(int limit) {
        List<DatasetSummary> results = new ArrayList<>();
        for (OpenDataSourcePort source : sources) {
            DatasetDefinition definition = source.definition();
            try {
                String json = source.fetchRecordsAsJson(limit);
                results.add(analytics.summarize(definition, json));
            } catch (OpenDataUnavailableException ex) {
                results.add(DatasetSummary.unavailable(definition, ex.getMessage()));
            } catch (AnalysisException ex) {
                results.add(DatasetSummary.analysisFailed(definition, ex.getMessage()));
            }
        }
        return results;
    }
}
