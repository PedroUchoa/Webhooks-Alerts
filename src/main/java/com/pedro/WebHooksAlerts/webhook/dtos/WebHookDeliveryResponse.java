package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.WebHookDelivery;
import com.pedro.WebHooksAlerts.webhook.enums.DeliveryStatus;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;

import java.time.Instant;
import java.util.UUID;

public record WebHookDeliveryResponse(UUID id,
                                      UUID subscriptionId,
                                      WebhookEventType eventType,
                                      DeliveryStatus status,
                                      Integer attemptCount,
                                      Integer lastHttpStatus,
                                      String lastError,
                                      Instant nextRetryAt,
                                      Instant createdAt,
                                      Instant updatedAt
) {
    public static WebHookDeliveryResponse from(WebHookDelivery d) {
        return new WebHookDeliveryResponse(
                d.getId(),
                d.getSubscription().getId(),
                d.getEventType(),
                d.getStatus(),
                d.getAttemptCount(),
                d.getLastHttpStatus(),
                d.getLastError(),
                d.getNextRetryAt(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
