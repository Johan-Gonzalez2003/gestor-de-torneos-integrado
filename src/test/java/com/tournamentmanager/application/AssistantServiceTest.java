package com.tournamentmanager.application;

import com.tournamentmanager.application.command.AskCommand;
import com.tournamentmanager.application.model.AssistantAnswer;
import com.tournamentmanager.application.model.KnowledgeDocument;
import com.tournamentmanager.application.model.PromptMode;
import com.tournamentmanager.application.port.out.KnowledgeBasePort;
import com.tournamentmanager.application.port.out.LanguageModelPort;
import com.tournamentmanager.application.service.AssistantService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssistantServiceTest {

    @Test
    void doesNotCallModelWhenNotConfigured() {
        LanguageModelPort llm = Mockito.mock(LanguageModelPort.class);
        KnowledgeBasePort kb = Mockito.mock(KnowledgeBasePort.class);
        when(llm.isConfigured()).thenReturn(false);
        when(kb.retrieve(anyString(), anyInt())).thenReturn(List.of(new KnowledgeDocument("Estados", "x")));

        AssistantAnswer answer = new AssistantService(llm, kb).ask(new AskCommand("¿Estados?", null));

        assertThat(answer.llmUsed()).isFalse();
        assertThat(answer.sources()).containsExactly("Estados");
        verify(llm, never()).generate(anyString(), anyString());
    }

    @Test
    void usesRetrievedContextAndRequestedMode() {
        LanguageModelPort llm = Mockito.mock(LanguageModelPort.class);
        KnowledgeBasePort kb = Mockito.mock(KnowledgeBasePort.class);
        when(llm.isConfigured()).thenReturn(true);
        when(kb.retrieve(eq("pregunta"), eq(3))).thenReturn(List.of());
        when(llm.generate(anyString(), eq("pregunta"))).thenReturn("respuesta");

        AssistantAnswer answer = new AssistantService(llm, kb).ask(new AskCommand("pregunta", PromptMode.COT));

        assertThat(answer.answer()).isEqualTo("respuesta");
        assertThat(answer.mode()).isEqualTo(PromptMode.COT);
        assertThat(answer.llmUsed()).isTrue();
    }
}
