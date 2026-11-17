package com.torneos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada unico del proyecto integrado.
 * Escanea los dos modulos: com.torneos (JWT/RBAC) y com.tournamentmanager (hexagonal, JPA, LLM, datos abiertos).
 */
@SpringBootApplication(scanBasePackages = {"com.torneos", "com.tournamentmanager"})
@EntityScan("com.tournamentmanager.infrastructure.persistence")
@EnableJpaRepositories("com.tournamentmanager.infrastructure.persistence")
public class TorneosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TorneosApplication.class, args);
    }
}
