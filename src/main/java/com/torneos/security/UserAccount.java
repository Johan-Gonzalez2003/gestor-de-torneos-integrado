package com.torneos.security;

/** Cuenta de usuario. Solo se guarda el hash de la contrasena, nunca el texto plano. */
public record UserAccount(String username, String passwordHash, Role role) {
}
