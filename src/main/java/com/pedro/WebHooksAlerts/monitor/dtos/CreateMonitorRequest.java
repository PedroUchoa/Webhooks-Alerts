package com.pedro.WebHooksAlerts.monitor.dtos;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public record CreateMonitorRequest(
        @NotBlank(message = "Name is Required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "targetUrl is Required")
        @URL(regexp = "^https?://.*", message = "The URL Must Start With http:// or https://")
        String targetUrl,

        @NotNull
        @Min(value = 30, message = "Min Interval is 30 seconds")
        @Max(value = 86400, message = "Max Interval is 24 hours")
        Integer intervalSeconds,

        @Min(1000)
        @Max(30000)
        Integer timeoutMs
        ) {}
