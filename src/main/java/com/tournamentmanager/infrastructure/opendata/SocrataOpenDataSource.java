package com.tournamentmanager.infrastructure.opendata;

import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.port.out.OpenDataSourcePort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP para portales Socrata (SODA API), que usa datos.gov.co y tambien publica datasets de DANE.
 * Endpoint: {baseUrl}/resource/{datasetId}.json?$limit=N
 */
public final class SocrataOpenDataSource implements OpenDataSourcePort {

    private final DatasetDefinition definition;
    private final String baseUrl;
    private final String datasetId;
    private final HttpClient httpClient;
    private final Duration timeout;

    public SocrataOpenDataSource(DatasetDefinition definition, String baseUrl, String datasetId,
                                 HttpClient httpClient, Duration timeout) {
        this.definition = definition;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.datasetId = datasetId;
        this.httpClient = httpClient;
        this.timeout = timeout;
    }

    @Override
    public DatasetDefinition definition() {
        return definition;
    }

    @Override
    public String fetchRecordsAsJson(int limit) {
        URI uri = URI.create(baseUrl + "/resource/" + datasetId + ".json?$limit=" + limit);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("HTTP " + response.statusCode() + " desde " + definition.displayName());
            }
            return response.body();
        } catch (IOException ex) {
            throw new IllegalStateException("Error de red con " + definition.displayName(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consulta interrumpida a " + definition.displayName(), ex);
        }
    }
}
