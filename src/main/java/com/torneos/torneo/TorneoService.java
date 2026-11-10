package com.torneos.torneo;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.torneos.common.ResourceNotFoundException;
import com.torneos.security.Actor;

/** Reglas de negocio de los torneos, incluido el control de propiedad (BOLA). */
@Service
public class TorneoService {

    static final int MAX_PAGE_SIZE = 50;

    private final TorneoRepository repository;

    public TorneoService(TorneoRepository repository) {
        this.repository = repository;
    }

    public TorneoResponse crear(TorneoRequest request, Actor actor) {
        Torneo nuevo = new Torneo(null, request.nombre(), request.videojuego(), request.fechaInicio(),
                request.maxParticipantes(), EstadoTorneo.PROGRAMADO, actor.username());
        return TorneoResponse.from(repository.save(nuevo));
    }

    public TorneoResponse buscar(long id) {
        return TorneoResponse.from(obtener(id));
    }

    public PageResponse<TorneoResponse> listar(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var content = repository.findPage(safePage, safeSize).stream()
                .map(TorneoResponse::from)
                .toList();
        return new PageResponse<>(content, safePage, safeSize, repository.count());
    }

    public TorneoResponse actualizar(long id, TorneoRequest request, Actor actor) {
        Torneo actual = obtener(id);
        verificarPropiedad(actual, actor);
        Torneo cambiado = actual.conDatos(request.nombre(), request.videojuego(),
                request.fechaInicio(), request.maxParticipantes());
        return TorneoResponse.from(repository.save(cambiado));
    }

    public void eliminar(long id, Actor actor) {
        verificarPropiedad(obtener(id), actor);
        repository.deleteById(id);
    }

    private Torneo obtener(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado: " + id));
    }

    /** Solo el administrador o quien creo el torneo puede modificarlo o borrarlo. */
    private void verificarPropiedad(Torneo torneo, Actor actor) {
        if (!actor.admin() && !torneo.creadoPor().equals(actor.username())) {
            throw new AccessDeniedException("No es el propietario del torneo");
        }
    }
}
