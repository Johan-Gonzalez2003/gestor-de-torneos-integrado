package com.tournamentmanager.application.port.out;

import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.model.DatasetSummary;

/** Puerto de salida para análisis descriptivo de un conjunto de registros (DuckDB en la implementación). */
public interface AnalyticsPort {
    DatasetSummary summarize(DatasetDefinition definition, String recordsJson);
}
