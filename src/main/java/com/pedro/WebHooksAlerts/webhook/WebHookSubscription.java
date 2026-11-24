package com.pedro.WebHooksAlerts.webhook;

import jakarta.persistence.*;

import java.util.HashSet;

public class WebHookSubscription {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "webhook_subscription_event",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private Set<WebHookEventType> events = new HashSet<>();

}
