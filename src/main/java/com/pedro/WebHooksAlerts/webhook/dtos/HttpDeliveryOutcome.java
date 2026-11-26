package com.pedro.WebHooksAlerts.webhook.dtos;

public record HttpDeliveryOutcome(boolean delivered, boolean retryable, Integer httpStatus, String error) {
   public static HttpDeliveryOutcome success(int status) {
        return new HttpDeliveryOutcome(true, false, status, null);
    }
    public  static HttpDeliveryOutcome httpError(int status, boolean retryable) {
        return new HttpDeliveryOutcome(false, retryable, status, "Server Returned HTTP " + status);
    }
    public static HttpDeliveryOutcome networkError(String error) {
        return new HttpDeliveryOutcome(false, true, null, error);
    }
}