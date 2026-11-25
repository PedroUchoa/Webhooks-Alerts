package com.pedro.WebHooksAlerts.webhook.dtos;

import com.pedro.WebHooksAlerts.webhook.WebHookDelivery;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

public record CreateWebHookRequest(
        @NotBlank(message = "Callback URL is Required")
        @URL@URL(regexp = "^https?://.*", message = "The Url Must Start With http:// or https://")
        String callbackUrl,

        @NotEmpty(message = "Must Have At Least One Event Type")
        Set<WebhookEventType> events
) {
}
