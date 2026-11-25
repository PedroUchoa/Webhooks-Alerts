package com.pedro.WebHooksAlerts.monitor.dtos;

import java.util.UUID;

public record MonitorTarget(UUID id, String name, String targetUrl, int timeoutMs) {
}
