package com.tournamentmanager.application.service;

import com.tournamentmanager.application.command.AskCommand;
import com.tournamentmanager.application.model.AssistantAnswer;
import com.tournamentmanager.application.model.KnowledgeDocument;
import com.tournamentmanager.application.model.PromptMode;
import com.tournamentmanager.application.port.out.KnowledgeBasePort;
import com.tournamentmanager.application.port.out.LanguageModelPort;

import java.util.List;

/** Asistente RAG: recupera contexto, arma el prompt según el modo y consulta el modelo. */
public class AssistantService {

    private static final int CONTEXT_DOCUMENTS = 3;

    private final LanguageModelPort languageModel;
    private final KnowledgeBasePort knowledgeBase;

    public AssistantService(LanguageModelPort languageModel, KnowledgeBasePort knowledgeBase) {
        this.languageModel = languageModel;
        this.knowledgeBase = knowledgeBase;
    }

    public AssistantAnswer ask(AskCommand command) {
        PromptMode mode = command.mode() == null ? PromptMode.ZERO_SHOT : command.mode();
        List<KnowledgeDocument> documents = knowledgeBase.retrieve(command.question(), CONTEXT_DOCUMENTS);
        List<String> sources = documents.stream().map(KnowledgeDocument::title).toList();

        if (!languageModel.isConfigured()) {
            String message = "El asistente de IA no está configurado: define OPENAI_API_KEY. "
                    + "Fuentes recuperadas: " + (sources.isEmpty() ? "ninguna." : String.join(", ", sources) + ".");
            return new AssistantAnswer(message, mode, sources, false);
        }

        String answer = languageModel.generate(buildSystemPrompt(mode, documents), command.question());
        return new AssistantAnswer(answer, mode, sources, true);
    }

    static String buildSystemPrompt(PromptMode mode, List<KnowledgeDocument> documents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Eres el asistente del gestor de torneos de videojuegos. Responde en español, ")
                .append("usando solo el contexto proporcionado cuando sea posible.\n");
        switch (mode) {
            case FEW_SHOT -> prompt.append("""
                    Sigue el formato de estos ejemplos:
                    Pregunta: ¿Qué estados existen?
                    Respuesta: DRAFT, REGISTRATION_OPEN, IN_PROGRESS, FINISHED y CANCELLED.
                    """);
            case COT -> prompt.append("""
                    Analiza el problema paso a paso de forma interna y entrega solo una conclusión breve
                    justificada. No expongas tu razonamiento interno completo.
                    """);
            default -> prompt.append("Responde de forma directa y concisa.\n");
        }
        if (!documents.isEmpty()) {
            prompt.append("Contexto:\n");
            for (KnowledgeDocument doc : documents) {
                prompt.append("- ").append(doc.title()).append(": ").append(doc.content()).append('\n');
            }
        }
        return prompt.toString();
    }
}
