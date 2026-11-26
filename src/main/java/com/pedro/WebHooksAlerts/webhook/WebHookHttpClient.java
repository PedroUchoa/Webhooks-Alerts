package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.config.WebHookProperties;
import com.pedro.WebHooksAlerts.webhook.dtos.HttpDeliveryOutcome;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class WebHookHttpClient {

    public static final String HEADER_SIGNATURE = "X-Webhook-Signature";
    public static final String HEADER_TIMESTAMP = "X-Webhook-Timestamp";
    public static final String HEADER_DELIVERY_ID = "X-Webhook-Delivery-Id";

    private final RestClient restClient;

    WebHookHttpClient(RestClient.Builder builder, WebHookProperties properties){
        HttpClient jdkClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.timeoutMs()));

        this.restClient = builder.clone()
                .requestFactory(requestFactory)
                .build();
    }

    public HttpDeliveryOutcome send(
            String callbackUrl,
            String rawPayload,
            String deliveryId,
            long timestamp,
            String signature
    ){
        try {
            return restClient.post()
                    .uri(callbackUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.USER_AGENT, "UptimeWebhookMonitor/1.0")
                    .header(HEADER_DELIVERY_ID, deliveryId)
                    .header(HEADER_TIMESTAMP, String.valueOf(timestamp))
                    .header(HEADER_SIGNATURE, signature)
                    .body(rawPayload)
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return HttpDeliveryOutcome.success(status);
                        }
                        boolean retryable = status == 429 || status >= 500;
                        return HttpDeliveryOutcome.httpError(status, retryable);
                    });
        } catch (ResourceAccessException ex) {
            String cause = ex.getMostSpecificCause().getMessage();
            return HttpDeliveryOutcome.networkError("network error/timeout: " + cause);
        } catch (Exception ex) {
            return HttpDeliveryOutcome.networkError("Unexpected error while sending: " + ex.getMessage());
        }
    }

}
