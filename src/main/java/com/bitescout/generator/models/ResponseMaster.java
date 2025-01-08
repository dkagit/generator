package com.bitescout.generator.models;

import lombok.AllArgsConstructor;

public class ResponseMaster {
    public static enum Status {
        SUCCESS, FAILURE
    }
    public record GenericResponse(
        Object data,
        Status status,
        String error
    ) {}
}