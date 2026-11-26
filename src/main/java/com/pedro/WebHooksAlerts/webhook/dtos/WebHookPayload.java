package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;

import java.time.Instant;
import java.util.UUID;

public record WebHookPayload (
        UUID eventId,
        WebhookEventType eventType,
        UUID monitorId,
        String monitorName,
        String targetUrl,
        Integer httpStatus,
        Integer latencyMs,
        String errorMessage,
        Instant occurredAt
) {}
