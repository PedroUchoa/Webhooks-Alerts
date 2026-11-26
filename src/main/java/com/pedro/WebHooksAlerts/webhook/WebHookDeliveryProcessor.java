package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.shared.exception.ConflictException;
import com.pedro.WebHooksAlerts.shared.exception.ResourceNotFoundException;
import com.pedro.WebHooksAlerts.webhook.config.WebHookProperties;
import com.pedro.WebHooksAlerts.webhook.dtos.DeliveryContext;
import com.pedro.WebHooksAlerts.webhook.dtos.HttpDeliveryOutcome;
import com.pedro.WebHooksAlerts.webhook.dtos.RetryDecision;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookDeliveryResponse;
import com.pedro.WebHooksAlerts.webhook.enums.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WebHookDeliveryProcessor {

    private final WebHookDeliveryRepository deliveryRepository;
    private final WebHookProperties properties;

    @Transactional(readOnly = true)
    public Optional<DeliveryContext> loadForProcessing(UUID deliveryId){
        return deliveryRepository.findByIdWithSubscription(deliveryId)
                .map(d-> new DeliveryContext(
                        d.getId(),
                        d.getStatus(),
                        d.getSubscription().getCallbackUrl(),
                        d.getSubscription().getSecretKey(),
                        d.getPayloadJson()
                ));
    }

    @Transactional
    public RetryDecision recordOutcome (UUID deliveryId, HttpDeliveryOutcome outcome){
        WebHookDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if(delivery == null || delivery.getStatus() == DeliveryStatus.DELIVERED) return RetryDecision.none();

        if (outcome.delivered()){
            delivery.markDelivered(outcome.httpStatus());
            deliveryRepository.save(delivery);
            return RetryDecision.none();
        }

        int currentAttempt = delivery.getAttemptCount() + 1;
        boolean hasAttemptLeft = currentAttempt < properties.maxAttempts() && currentAttempt <= properties.retryDelaysMs().size();

        if (outcome.retryable() && hasAttemptLeft){
            long baseDelayMs = properties.retryDelaysMs().get(currentAttempt -1);
            long delayWithJitterMs = applyJitter(baseDelayMs);
            Instant nextRetryAt = Instant.now().plusMillis(delayWithJitterMs);

            delivery.markRetying(outcome.httpStatus(), outcome.error(), nextRetryAt);
            deliveryRepository.save(delivery);
            return RetryDecision.retry(currentAttempt,delayWithJitterMs);
        }

        delivery.markFailed(outcome.httpStatus(), outcome.error());
        deliveryRepository.save(delivery);
        return RetryDecision.sendToDlq();
    }

    @Transactional
    public WebHookDeliveryResponse prepareManualRetry(UUID deliveryId){
        WebHookDelivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(()->new ResourceNotFoundException("WebHook Not Found: " + deliveryId));

        if (delivery.getStatus() != DeliveryStatus.FAILED){
            throw new ConflictException("Only Delivery's with Status FAILED can be Resented Manually, Status: " + delivery.getStatus());
        }

        delivery.resetForManualRetry();
        return WebHookDeliveryResponse.from(deliveryRepository.save(delivery));

    }

    private long applyJitter(long baseDelayMs) {
        double factor = properties.jitterFactor();
        if (factor <= 0) {
            return baseDelayMs;
        }
        long minDelay = Math.max(1L, (long) (baseDelayMs * (1.0 - factor)));
        return ThreadLocalRandom.current().nextLong(minDelay, baseDelayMs + 1);
    }




}
