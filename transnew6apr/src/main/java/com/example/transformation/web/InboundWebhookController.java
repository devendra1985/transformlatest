package com.example.transformation.web;

import com.example.transformation.dto.inbound.WebhookAckResponse;
import com.example.transformation.processor.ExchangeKeys;
import java.time.Instant;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for receiving inbound payment webhook notifications from third-party vendors.
 *
 * <p>End-to-end flow:
 * <pre>
 *   Vendor ──POST──► Webhook ──► Gateway ──► POST /api/inbound/webhook/{vendorId}/{cartridgeId}
 *                                                    │
 *                                         InboundWebhookController
 *                                                    │  ProducerTemplate.request()
 *                                         Camel: direct:inbound-{cartridgeId}
 *                                                    │
 *                                         InboundPersistProcessor
 *                                          ├─ persist raw payload → DB (status: INBOUND_RECEIVED)
 *                                          └─ build WebhookAckResponse
 *                                                    │
 *                                         ◄─ 202 Accepted + WebhookAckResponse (JSON)
 * </pre>
 *
 * <p>Request headers forwarded to the Camel exchange:
 * <ul>
 *   <li>{@code X-Correlation-Id} – used as primary correlationId / DB key</li>
 *   <li>{@code X-Vendor-Signature} – HMAC-SHA256 from the vendor (for future verification)</li>
 *   <li>{@code X-Webhook-Event} – vendor event type (e.g. {@code PAYMENT_INBOUND})</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/inbound")
public class InboundWebhookController {

    private static final Logger log = LoggerFactory.getLogger(InboundWebhookController.class);

    private final ProducerTemplate producerTemplate;

    public InboundWebhookController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    /**
     * Receives an inbound payment webhook notification from a vendor forwarded by the gateway.
     *
     * <p>On success the payload is persisted to the DB and a {@code 202 Accepted} with a
     * {@link WebhookAckResponse} is returned so the gateway can confirm delivery.
     *
     * @param vendorId         vendor identifier (e.g. {@code VISA}) – path variable
     * @param cartridgeId      cartridge identifier (e.g. {@code VISABA}) – determines Camel route
     * @param correlationId    optional client-supplied correlation ID ({@code X-Correlation-Id})
     * @param vendorSignature  optional HMAC-SHA256 payload signature ({@code X-Vendor-Signature})
     * @param webhookEvent     optional vendor event type ({@code X-Webhook-Event})
     * @param body             raw vendor payload
     * @return 202 Accepted with {@link WebhookAckResponse}
     */
    @PostMapping(
            value = "/webhook/{vendorId}/{cartridgeId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebhookAckResponse> receiveWebhook(
            @PathVariable String vendorId,
            @PathVariable String cartridgeId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader(value = "X-Vendor-Signature", required = false) String vendorSignature,
            @RequestHeader(value = "X-Webhook-Event", required = false) String webhookEvent,
            @RequestBody Map<String, Object> body) {

        log.info("Inbound webhook: vendor={}, cartridge={}, event={}, correlationId={}",
                vendorId, cartridgeId, webhookEvent, correlationId);

        String endpoint = "direct:inbound-" + cartridgeId;

        Exchange out = producerTemplate.request(endpoint, e -> {
            e.getMessage().setBody(body);
            e.getMessage().setHeader(ExchangeKeys.VENDOR_ID_HEADER, vendorId);
            e.getMessage().setHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, cartridgeId);
            e.getMessage().setHeader(ExchangeKeys.DIRECTION_HEADER, ExchangeKeys.DIRECTION_INBOUND);
            if (webhookEvent != null) {
                e.getMessage().setHeader(ExchangeKeys.WEBHOOK_EVENT_HEADER, webhookEvent);
            }
            if (vendorSignature != null) {
                e.getMessage().setHeader(ExchangeKeys.VENDOR_SIGNATURE_HEADER, vendorSignature);
            }
            if (correlationId != null && !correlationId.isBlank()) {
                e.getMessage().setHeader("X-Correlation-Id", correlationId);
            }
        });

        if (out.getException() != null) {
            log.error("Inbound webhook processing failed for cartridge={}: ", cartridgeId, out.getException());
            String failedCorrelationId = out.getMessage().getHeader("X-Request-Id", String.class);
            WebhookAckResponse rejected = WebhookAckResponse.rejected(
                    failedCorrelationId != null ? failedCorrelationId : "UNKNOWN",
                    "Webhook processing failed: " + out.getException().getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rejected);
        }

        WebhookAckResponse ack = out.getMessage().getBody(WebhookAckResponse.class);
        String requestId = out.getMessage().getHeader("X-Request-Id", String.class);

        log.info("Inbound webhook accepted: correlationId={}", ack != null ? ack.correlationId() : requestId);

        return ResponseEntity.accepted()
                .header("X-Request-Id", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ack);
    }

    /**
     * Lightweight health check confirming the inbound webhook endpoint is reachable.
     * Used by the gateway or load-balancer to verify service availability.
     */
    @GetMapping(value = "/webhook/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "transformation-service",
                "timestamp", Instant.now().toString()));
    }
}
