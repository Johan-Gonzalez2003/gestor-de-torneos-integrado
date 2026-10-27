package com.tournamentmanager.infrastructure.knowledge;

import com.tournamentmanager.application.model.KnowledgeDocument;
import com.tournamentmanager.application.port.out.KnowledgeBasePort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** RAG básico por coincidencia de términos. Para producción conviene embeddings y una base vectorial. */
@Component
public class InMemoryKnowledgeBaseAdapter implements KnowledgeBasePort {

    private final List<KnowledgeDocument> documents = List.of(
            new KnowledgeDocument("Reglas de eliminación directa",
                    "En eliminación directa, el equipo que pierde un enfrentamiento queda eliminado. "
                            + "El ganador avanza a la siguiente ronda. El torneo termina cuando se completa la final."),
            new KnowledgeDocument("Inscripciones y cupos",
                    "Un equipo solo debe inscribirse una vez por torneo. No se aceptan nuevas inscripciones "
                            + "cuando el torneo no tiene inscripciones abiertas o el cupo máximo está lleno. "
                            + "Para iniciar un torneo se requieren al menos dos equipos."),
            new KnowledgeDocument("Estados del torneo",
                    "Los estados disponibles son DRAFT, REGISTRATION_OPEN, IN_PROGRESS, FINISHED y CANCELLED. "
                            + "Representan borrador, inscripciones abiertas, en curso, finalizado y cancelado."),
            new KnowledgeDocument("Datos abiertos colombianos",
                    "El gestor consulta datos.gov.co y el DANE a través de la API Socrata. Cada consulta pasa "
                            + "por un circuit breaker y reintentos con backoff exponencial. Los datos se analizan con DuckDB."),
            new KnowledgeDocument("Arquitectura del proyecto",
                    "El proyecto usa arquitectura hexagonal: domain, application, api e infrastructure. "
                            + "Las dependencias apuntan hacia el dominio y se inyectan por constructor.")
    );

    @Override
    public List<KnowledgeDocument> retrieve(String question, int limit) {
        Set<String> terms = tokenize(question);
        if (terms.isEmpty()) {
            return documents.stream().limit(limit).toList();
        }
        return documents.stream()
                .map(doc -> Map.entry(doc, score(doc, terms)))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<KnowledgeDocument, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private int score(KnowledgeDocument document, Set<String> terms) {
        Set<String> docTerms = tokenize(document.title() + " " + document.content());
        return (int) terms.stream().filter(docTerms::contains).count();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                        .split("\\s+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }
}
