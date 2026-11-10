package com.torneos.torneo;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia. Cambiar a JPA/PostgreSQL solo exige otra implementacion. */
public interface TorneoRepository {

    Torneo save(Torneo torneo);

    Optional<Torneo> findById(long id);

    /** Devuelve una pagina ordenada por id. */
    List<Torneo> findPage(int page, int size);

    long count();

    void deleteById(long id);
}
