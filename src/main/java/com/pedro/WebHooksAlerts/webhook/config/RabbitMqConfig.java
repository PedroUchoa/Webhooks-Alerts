package com.pedro.WebHooksAlerts.webhook.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "webhook.exchange";
    public static final String DELIVERY_QUEUE = "webhook.delivery.queue";
    public static final String DELIVERY_ROUTING_KEY = "webhook.deliver";
    public static final String DLQ_QUEUE = "webhook.dlq";
    public static final String DLQ_ROUTING_KEY = "webhook.dlq";

    public static String retryRoutingKey(int tier){
        return "webhook.retry." + tier;
    }

    @Bean
    public Declarables webhookTopology(WebHookProperties properties){
        DirectExchange exchange = new DirectExchange(EXCHANGE, true, false);

        Queue deliveryQueue = QueueBuilder.durable(DELIVERY_QUEUE).build();
        Binding deliveryBinding = BindingBuilder.bind(deliveryQueue)
                .to(exchange)
                .with(DELIVERY_ROUTING_KEY);

        Queue dlq = QueueBuilder.durable(DLQ_QUEUE).build();
        Binding dlqBinding = BindingBuilder.bind(dlq)
                .to(exchange)
                .with(DLQ_ROUTING_KEY);

        List<Declarable> declarable = new ArrayList<>();
        declarable.add(exchange);
        declarable.add(deliveryQueue);
        declarable.add(deliveryBinding);
        declarable.add(dlq);
        declarable.add(dlqBinding);

        List<Long> delays = properties.retryDelaysMs();
        for (int i = 0; i < delays.size(); i++){
            int tier = i+1;
            String queueName = retryRoutingKey(tier);

            Queue retryQueue = QueueBuilder.durable(queueName)
                    .withArgument("x-dead-letter-exchange", EXCHANGE)
                    .withArgument("x-dead-letter-routing-key", DELIVERY_ROUTING_KEY)
                    .withArgument("x-message-ttl", delays.get(i))
                    .build();

            Binding retryBinding = BindingBuilder.bind(retryQueue)
                    .to(exchange)
                    .with(queueName);

            declarable.add(retryQueue);
            declarable.add(retryBinding);
        }

        return new Declarables(declarable);




    }




}
