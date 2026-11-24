package com.pedro.WebHooksAlerts.monitor.dtos;

import jakarta.validation.constraints.NotNull;

public record UpdateMonitorStatusRequest(
        @NotNull(message = "The Active Is Required")
        Boolean active
) {
}
