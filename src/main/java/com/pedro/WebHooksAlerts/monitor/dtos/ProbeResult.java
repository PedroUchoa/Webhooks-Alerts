package com.pedro.WebHooksAlerts.monitor.dtos;

public record ProbeResult(Integer HttpStatus, int latencyMs, boolean success, String errorMessage) {
}
