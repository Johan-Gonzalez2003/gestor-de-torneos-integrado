package com.tournamentmanager.application.command;

import com.tournamentmanager.application.model.PromptMode;

public record AskCommand(String question, PromptMode mode) {
}
