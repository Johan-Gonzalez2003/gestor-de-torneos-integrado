package com.tournamentmanager.api.rest;

import com.tournamentmanager.api.dto.TournamentRequest;
import com.tournamentmanager.api.dto.TournamentResponse;
import com.tournamentmanager.application.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@Tag(name = "Torneos", description = "Operaciones CRUD para torneos de videojuegos.")
public class TournamentController {

    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar torneos")
    public List<TournamentResponse> findAll() {
        return service.findAll().stream().map(TournamentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un torneo por ID")
    public TournamentResponse findById(@PathVariable Long id) {
        return TournamentResponse.from(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear un torneo")
    public ResponseEntity<TournamentResponse> create(@Valid @RequestBody TournamentRequest request) {
        TournamentResponse created = TournamentResponse.from(service.create(request.toCommand()));
        return ResponseEntity.created(URI.create("/api/tournaments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un torneo")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request) {
        return TournamentResponse.from(service.update(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un torneo")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
