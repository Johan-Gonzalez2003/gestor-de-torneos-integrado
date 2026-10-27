package com.tournamentmanager.application.model;

import java.util.List;

public record AssistantAnswer(String answer, PromptMode mode, List<String> sources, boolean llmUsed) {
}
