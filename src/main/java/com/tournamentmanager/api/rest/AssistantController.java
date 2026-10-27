package com.tournamentmanager.api.rest;

import com.tournamentmanager.api.dto.AskRequest;
import com.tournamentmanager.api.dto.AskResponse;
import com.tournamentmanager.application.service.AssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@Tag(name = "Asistente IA", description = "Asistente LLM con prompt engineering y recuperación RAG básica.")
public class AssistantController {

    private final AssistantService service;

    public AssistantController(AssistantService service) {
        this.service = service;
    }

    @PostMapping("/ask")
    @Operation(summary = "Consultar al asistente sobre reglas y funcionamiento del gestor")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return AskResponse.from(service.ask(request.toCommand()));
    }
}
