package com.pedro.WebHooksAlerts.monitor.dtos;

import com.pedro.WebHooksAlerts.monitor.MonitorStatus;

import java.time.Instant;
import java.util.UUID;

public record MonitorStatusChangedEvent(
        UUID monitorId,
        String monitorName,
        String targetUrl,
        MonitorStatus newStatus,
        Integer httpStatus,
        Integer latencyMs,
        String errorMessage,
        Instant occurredAt
) {
}
