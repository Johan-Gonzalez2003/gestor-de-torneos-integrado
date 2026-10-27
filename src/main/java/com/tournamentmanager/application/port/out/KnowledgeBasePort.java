package com.tournamentmanager.application.port.out;

import com.tournamentmanager.application.model.KnowledgeDocument;

import java.util.List;

/** Puerto de salida para la recuperación de contexto (RAG). */
public interface KnowledgeBasePort {
    List<KnowledgeDocument> retrieve(String question, int limit);
}
