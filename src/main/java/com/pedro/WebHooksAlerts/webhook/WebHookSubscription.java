package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "webhook_subscriptions")
@Getter
@NoArgsConstructor
public class WebHookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "callback_url", nullable = false, length = 500)
    private String callbackUrl;

    @Column(name = "secret_key", nullable = false, length = 64)
    private String secretKey;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "webhook_subscription_events",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private Set<WebhookEventType> events = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WebHookSubscription(UUID monitorID, String callbackUrl, String secretKey, Set<WebhookEventType> events){
        this.monitorId = monitorID;
        this.callbackUrl = callbackUrl;
        this.secretKey = secretKey;
        this.events = new HashSet<>(events);
        this.createdAt = Instant.now();
    }

    public void deactivate(){
        this.active = false;
    }

}
