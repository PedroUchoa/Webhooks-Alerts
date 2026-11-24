package com.pedro.WebHooksAlerts.monitor.dtos;

import com.pedro.WebHooksAlerts.monitor.Monitor;
import com.pedro.WebHooksAlerts.monitor.MonitorStatus;

import java.time.Instant;
import java.util.UUID;

public record MonitorResponse(
        UUID Id,
        String name,
        String targetUrl,
        Integer intervalSeconds,
        Integer timeoutMs,
        MonitorStatus lastStatus,
        Integer consecutiveFailures,
        Instant nextCheckAt,
        Boolean active,
        Instant createdAt
) {
    public static MonitorResponse from(Monitor monitor) {
        return new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getTargetUrl(),
                monitor.getIntervalSeconds(),
                monitor.getTimeoutMs(),
                monitor.getLastStatus(),
                monitor.getConsecutiveFailures(),
                monitor.getNextCheckAt(),
                monitor.getActive(),
                monitor.getCreatedAt()
        );
    }
}
