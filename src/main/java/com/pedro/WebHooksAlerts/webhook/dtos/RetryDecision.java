package com.pedro.WebHooksAlerts.webhook.dtos;

public record RetryDecision(Action action, int retryTier, long delayMs) {
    public enum Action { NONE, RETRY, DLQ }
    public static RetryDecision none() { return new RetryDecision(Action.NONE, 0, 0); }
    public static RetryDecision retry(int tier, long delayMs) { return new RetryDecision(Action.RETRY, tier, delayMs); }
    public static RetryDecision sendToDlq() { return new RetryDecision(Action.DLQ, 0, 0); }
}
