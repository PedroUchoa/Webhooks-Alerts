package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.config.RabbitMqConfig;
import com.pedro.WebHooksAlerts.webhook.dtos.DeliveryContext;
import com.pedro.WebHooksAlerts.webhook.dtos.HttpDeliveryOutcome;
import com.pedro.WebHooksAlerts.webhook.dtos.RetryDecision;
import com.pedro.WebHooksAlerts.webhook.enums.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookDeliveryWorker {

    private final WebHookDeliveryProcessor processor;
    private final HmacSigner hmacSigner;
    private final WebHookHttpClient httpClient;
    private final WebHookQueuePublisher queuePublisher;

    @RabbitListener(queues = RabbitMqConfig.DELIVERY_QUEUE)
    public void consume(String rawDeliveryId) {
        UUID deliveryId = UUID.fromString(rawDeliveryId);

        DeliveryContext context = processor.loadForProcessing(deliveryId).orElse(null);
        if (context == null) {
            log.warn("WebhookDelivery {} Not Found In The Database; Mensage Deleted", deliveryId);
            return;
        }

        if (context.status() == DeliveryStatus.DELIVERED) {
            log.debug("WebhookDelivery {} Was Deliveryed Previously; Ignoring", deliveryId);
            return;
        }

        long timeStamp = Instant.now().getEpochSecond();
        String signature = hmacSigner.sign(timeStamp, context.payloadJson(), context.secretKey());

        HttpDeliveryOutcome outcome = httpClient.send(
                context.callbackUrl(),
                context.payloadJson(),
                deliveryId.toString(),
                timeStamp,
                signature
        );

        RetryDecision decision = processor.recordOutcome(deliveryId,outcome);

        switch (decision.action()) {
            case RETRY -> {
                log.warn(
                        "Falha temporária na entrega {} (HTTP {}). Reagendando nível {} em {}ms",
                        deliveryId, outcome.httpStatus(), decision.retryTier(), decision.delayMs()
                );
                queuePublisher.publishForRetry(deliveryId, decision.retryTier(), decision.delayMs());
            }
            case DLQ -> {
                log.error(
                        "Entrega {} falhou definitivamente (HTTP {}, motivo={}). Enviando para DLQ",
                        deliveryId, outcome.httpStatus(), outcome.error()
                );
                queuePublisher.publishToDlq(deliveryId);
            }
            case NONE ->
                    log.info("WebhookDelivery {} Successful Delivered (HTTP {})", deliveryId, outcome.httpStatus());
        }


    }

}
