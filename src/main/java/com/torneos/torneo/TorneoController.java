package com.torneos.torneo;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.torneos.security.Actor;

import jakarta.validation.Valid;

/** CRUD de torneos: 5 endpoints. */
@RestController
@RequestMapping("/api/v1/torneos")
public class TorneoController {

    private final TorneoService service;

    public TorneoController(TorneoService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TorneoResponse> listar(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.listar(page, size);
    }

    @GetMapping("/{id}")
    public TorneoResponse buscar(@PathVariable long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoResponse crear(@Valid @RequestBody TorneoRequest request, Authentication authentication) {
        return service.crear(request, Actor.from(authentication));
    }

    @PutMapping("/{id}")
    public TorneoResponse actualizar(@PathVariable long id, @Valid @RequestBody TorneoRequest request,
                                     Authentication authentication) {
        return service.actualizar(id, request, Actor.from(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id, Authentication authentication) {
        service.eliminar(id, Actor.from(authentication));
    }
}
