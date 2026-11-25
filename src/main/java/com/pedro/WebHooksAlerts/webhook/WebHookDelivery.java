package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.enums.DeliveryStatus;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@NoArgsConstructor
public class WebHookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private WebHookSubscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private WebhookEventType eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_http_status")
    private Integer lastHttpStatus;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public WebHookDelivery (WebHookSubscription subscription, WebhookEventType eventType, String payloadJson){
        this.subscription = subscription;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markDelivered(int httpStatus){
        this.attemptCount++;
        this.status =DeliveryStatus.DELIVERED;
        this.lastHttpStatus = httpStatus;
        this.lastError = null;
        this.nextRetryAt = null;
        this.updatedAt = Instant.now();
    }

    public void markRetying(Integer httpStatus, String error, Instant nextRetryAt){
        this.attemptCount++;
        this.status =DeliveryStatus.RETRYING;
        this.lastHttpStatus = httpStatus;
        this.lastError = truncateError(error);
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = Instant.now();
    }

    public void markFailed(Integer httpStatus, String error){
        this.attemptCount++;
        this.status =DeliveryStatus.FAILED;
        this.lastHttpStatus = httpStatus;
        this.lastError = truncateError(error);
        this.nextRetryAt = null;
        this.updatedAt = Instant.now();
    }

    public void resetForManualRetry(){
        this.status =DeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.lastError = null;
        this.nextRetryAt = null;
        this.updatedAt = Instant.now();
    }

    private String truncateError(String error) {
        if(error == null) return null;
        return error.length() > 500 ? error.substring(0,500) : error;
    }


}
