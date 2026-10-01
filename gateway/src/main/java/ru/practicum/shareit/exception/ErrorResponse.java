package ru.practicum.shareit.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {
    private final String error;
    private final List<String> details;

    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = details;
    }

}
