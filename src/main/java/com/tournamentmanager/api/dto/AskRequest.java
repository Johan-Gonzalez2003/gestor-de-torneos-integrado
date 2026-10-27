package com.tournamentmanager.api.dto;

import com.tournamentmanager.application.command.AskCommand;
import com.tournamentmanager.application.model.PromptMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @NotBlank(message = "La pregunta es obligatoria.")
        @Size(max = 500, message = "La pregunta no puede superar 500 caracteres.")
        String question,
        PromptMode mode) {

    public AskCommand toCommand() {
        return new AskCommand(question, mode);
    }
}
