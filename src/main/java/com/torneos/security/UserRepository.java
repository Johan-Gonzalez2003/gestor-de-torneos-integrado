package com.torneos.security;

import java.util.Optional;

/** Contrato de acceso a usuarios. Hoy es en memoria; manana puede ser JPA sin tocar el resto. */
public interface UserRepository {

    Optional<UserAccount> findByUsername(String username);
}
