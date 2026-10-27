package com.tournamentmanager.application.port.out;

/** Puerto de salida para cualquier proveedor de modelos de lenguaje. */
public interface LanguageModelPort {
    boolean isConfigured();
    String generate(String systemPrompt, String userQuestion);
}
