package com.torneos.security;

/** Datos que se leen de un JWT valido. */
public record TokenClaims(String username, Role role) {
}
