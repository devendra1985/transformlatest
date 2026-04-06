package com.example.transformation.dto.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * Acknowledgment response returned to the gateway after an inbound webhook payload is persisted.
 *
 * <p>Status values:
 * <ul>
 *   <li>{@code ACCEPTED} – payload was received and persisted successfully</li>
 *   <li>{@code REJECTED} – payload was received but could not be persisted (error path)</li>
 * </ul>
 */
public record WebhookAckResponse(
    String status,
    String correlationId,
    String message,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp
) {

    public static WebhookAckResponse accepted(String correlationId) {
        return new WebhookAckResponse("ACCEPTED", correlationId, "Payload received and persisted", Instant.now());
    }

    public static WebhookAckResponse rejected(String correlationId, String reason) {
        return new WebhookAckResponse("REJECTED", correlationId, reason, Instant.now());
    }
}
