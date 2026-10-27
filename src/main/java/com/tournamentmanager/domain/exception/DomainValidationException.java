package com.tournamentmanager.domain.exception;

import java.util.List;

public class DomainValidationException extends DomainException {
    private final List<String> errors;

    public DomainValidationException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
