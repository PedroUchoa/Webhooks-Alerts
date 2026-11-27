package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.WebHookSubscription;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WebHookCreatedResponse(
        UUID id,
        UUID monitorId,
        String callbackUrl,
        String secretKey,
        Set<WebhookEventType> events,
        Boolean active,
        Instant createdAt
) {

    public static WebHookCreatedResponse from(WebHookSubscription subscription){
        return new WebHookCreatedResponse(
                subscription.getId(),
                subscription.getMonitorId(),
                subscription.getCallbackUrl(),
                subscription.getSecretKey(),
                Set.copyOf(subscription.getEvents()),
                subscription.getActive(),
                subscription.getCreatedAt()
        );
    }

}
