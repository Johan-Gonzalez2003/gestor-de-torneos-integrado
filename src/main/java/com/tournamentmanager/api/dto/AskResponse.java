package com.tournamentmanager.api.dto;

import com.tournamentmanager.application.model.AssistantAnswer;
import com.tournamentmanager.application.model.PromptMode;

import java.util.List;

public record AskResponse(String answer, PromptMode mode, List<String> sources, boolean llmUsed) {

    public static AskResponse from(AssistantAnswer answer) {
        return new AskResponse(answer.answer(), answer.mode(), answer.sources(), answer.llmUsed());
    }
}
