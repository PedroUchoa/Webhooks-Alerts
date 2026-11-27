package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.ProbeResult;
import jakarta.servlet.annotation.HttpConstraint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class HttpProbeClient {

    private final RestClient.Builder restClientBuilder;

    private final HttpClient sharedHttpClient;

    HttpProbeClient(RestClient.Builder restClientBuilder, HttpClient monitorJdkHttpClient){
        this.restClientBuilder = restClientBuilder;
        this.sharedHttpClient = monitorJdkHttpClient;
    }

    public ProbeResult check(String targetUrl, int timeoutMs){
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(sharedHttpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        RestClient client = restClientBuilder.clone()
                .requestFactory(requestFactory)
                .build();

        long startNanos = System.nanoTime();

        try {
            return client.get()
                    .uri(targetUrl)
                    .header(HttpHeaders.USER_AGENT, "UptimeWebhookMonitor/1.0")
                    .exchange((request, response)->{
                        int statusCode = response.getStatusCode().value();
                        boolean success = response.getStatusCode().is2xxSuccessful();
                        int latencyMs = elapseMs(startNanos);
                        String error = success ? null : "Http Stats Unexpected? " + statusCode;
                        return new ProbeResult(statusCode, latencyMs, success, error);
                    });
        }catch (ResourceAccessException ex){
            int latencyMs = elapseMs(startNanos);
            String message = ex.getMostSpecificCause().getMessage();
            return new ProbeResult(null,latencyMs,false,truncate( "Connection Failed/Timeout: " + ex.getMessage()));
        }catch (Exception ex){
            int latencyMs = elapseMs(startNanos);
            return new ProbeResult(null,latencyMs,false,truncate("Unexpected Error: " + ex.getMessage()));
        }

    }

    private int elapseMs(long startNanos){
        return (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String truncate(String text){
        if (text == null) return null;
        return text.length() > 500 ? text.substring(0,500) : text;
    }

}
