package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.WebHookSubscription;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WebHookResponse(
        UUID id,
        UUID monitorId,
        String callbackUrl,
        Set<WebhookEventType> events,
        Boolean active,
        Instant createdAt
) {

    public static WebHookResponse from(WebHookSubscription subscription){
        return new WebHookResponse(
                subscription.getId(),
                subscription.getMonitorID(),
                subscription.getCallbackUrl(),
                Set.copyOf(subscription.getEvents()),
                subscription.getActive(),
                subscription.getCreatedAt()
        );
    }

}
