package com.pedro.WebHooksAlerts.monitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "app.monitor.scheduler-enable",
        havingValue = "true",
        matchIfMissing = true
)
public class ScheduleConfig {
}
