package com.torneos.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/** Dos usuarios de demostracion (admin y user) con contrasenas tomadas del entorno. */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserAccount> users;

    public InMemoryUserRepository(PasswordEncoder encoder,
                                  @Value("${app.seed.admin-password}") String adminPassword,
                                  @Value("${app.seed.user-password}") String userPassword) {
        this.users = Map.of(
                "admin", new UserAccount("admin", encoder.encode(adminPassword), Role.ADMIN),
                "user", new UserAccount("user", encoder.encode(userPassword), Role.USER),
                "user2", new UserAccount("user2", encoder.encode(userPassword), Role.USER));
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
