package com.pedro.WebHooksAlerts.monitor.dtos;

import com.pedro.WebHooksAlerts.monitor.CheckExecution;

import java.time.Instant;
import java.util.UUID;

public record CheckExecutionResponse(
        UUID id,
        Integer httpStatus,
        Integer latencyMs,
        Boolean success,
        String errorMessage,
        Instant checkedAt
) {
    public static CheckExecutionResponse from(CheckExecution execution) {
        return new CheckExecutionResponse(
                execution.getId(),
                execution.getHttpStatus(),
                execution.getLatencyMS(),
                execution.getSuccess(),
                execution.getErrorMessage(),
                execution.getCheckedAt()
        );
    }
}