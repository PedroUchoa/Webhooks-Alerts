package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class WebHookQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishForDelivery(UUID deliveryId){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.DELIVERY_ROUTING_KEY,
                deliveryId.toString()
        );
    }

    public void publishForRetry(UUID deliveryId, int retryTier, long delayWithJitterMs){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.retryRoutingKey(retryTier),
                deliveryId.toString(),
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(delayWithJitterMs));
                    return message;
                }
        );

    }

    public void publishToDlq(UUID deliveryId){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.DLQ_ROUTING_KEY,
                deliveryId.toString()
        );
    }


}
