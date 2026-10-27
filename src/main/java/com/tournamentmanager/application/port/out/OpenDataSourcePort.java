package com.tournamentmanager.application.port.out;

import com.tournamentmanager.application.model.DatasetDefinition;

/** Puerto de salida para una fuente de datos abiertos (datos.gov.co, DANE, ...). */
public interface OpenDataSourcePort {
    DatasetDefinition definition();

    /** Devuelve los registros como un arreglo JSON. Lanza OpenDataUnavailableException si la fuente falla. */
    String fetchRecordsAsJson(int limit);
}
