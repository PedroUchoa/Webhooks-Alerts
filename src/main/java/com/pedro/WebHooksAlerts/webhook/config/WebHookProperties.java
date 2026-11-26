package com.pedro.WebHooksAlerts.webhook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.webhook")
public record WebHookProperties(
        int maxAttempts,
        int timeoutMs,
        double jitterFactor,
        List<Long> retryDelaysMs
) {
}
