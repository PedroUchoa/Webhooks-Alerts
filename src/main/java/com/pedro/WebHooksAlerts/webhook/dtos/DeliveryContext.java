package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.enums.DeliveryStatus;

import java.util.UUID;

public record DeliveryContext(
        UUID id,
        DeliveryStatus status,
        String callbackUrl,
        String secretKey,
        String payloadJson
) {}