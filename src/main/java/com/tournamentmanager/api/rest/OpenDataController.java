package com.tournamentmanager.api.rest;

import com.tournamentmanager.application.model.DatasetSummary;
import com.tournamentmanager.application.service.OpenDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opendata")
@Tag(name = "Datos abiertos", description = "Consulta datos.gov.co y DANE, y los analiza con DuckDB.")
public class OpenDataController {

    private static final int MAX_LIMIT = 1000;

    private final OpenDataService service;

    public OpenDataController(OpenDataService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen descriptivo de cada fuente de datos abiertos")
    public List<DatasetSummary> summary(@RequestParam(defaultValue = "100") int limit) {
        int bounded = Math.max(1, Math.min(limit, MAX_LIMIT));
        return service.summarizeAll(bounded);
    }
}
