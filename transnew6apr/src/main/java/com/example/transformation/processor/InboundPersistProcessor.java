package com.example.transformation.processor;

import com.example.transformation.dto.inbound.WebhookAckResponse;
import com.example.transformation.persistence.PayloadPersistenceService;
import java.util.Map;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Camel processor for the inbound webhook route.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Resolve (or generate) a {@code correlationId} from the inbound payload / headers.</li>
 *   <li>Persist the raw vendor payload to the DB with status {@code INBOUND_RECEIVED}.</li>
 *   <li>Set a {@link WebhookAckResponse} as the Camel exchange body so the controller can
 *       return it as the HTTP response to the gateway.</li>
 * </ol>
 *
 * <p>Persistence is guarded by the {@code app.persistence.enabled} flag. When disabled the
 * processor still resolves the correlationId and builds the ack, but skips the DB write.
 */
@Component("inboundPersist")
public class InboundPersistProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(InboundPersistProcessor.class);

    private static final String STATUS_INBOUND_RECEIVED = "INBOUND_RECEIVED";

    private final ObjectProvider<PayloadPersistenceService> persistenceService;
    private final boolean persistenceEnabled;

    public InboundPersistProcessor(
            ObjectProvider<PayloadPersistenceService> persistenceService,
            @Value("${app.persistence.enabled:false}") boolean persistenceEnabled) {
        this.persistenceService = persistenceService;
        this.persistenceEnabled = persistenceEnabled;
    }

    @Override
    public void process(Exchange exchange) {
        Object body = exchange.getMessage().getBody();
        String correlationId = resolveCorrelationId(exchange, body);

        exchange.getMessage().setHeader("X-Request-Id", correlationId);

        String vendorId = exchange.getMessage().getHeader(ExchangeKeys.VENDOR_ID_HEADER, String.class);
        String cartridgeId = exchange.getMessage().getHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, String.class);
        String webhookEvent = exchange.getMessage().getHeader(ExchangeKeys.WEBHOOK_EVENT_HEADER, String.class);

        log.info("Inbound webhook received: correlationId={}, vendor={}, cartridge={}, event={}",
                correlationId, vendorId, cartridgeId, webhookEvent);

        if (persistenceEnabled) {
            PayloadPersistenceService svc = persistenceService.getIfAvailable();
            if (svc != null) {
                svc.storeRaw(correlationId, body, STATUS_INBOUND_RECEIVED);
                log.debug("Persisted inbound payload: correlationId={}", correlationId);
            }
        } else {
            log.debug("Persistence disabled – skipping DB write for correlationId={}", correlationId);
        }

        exchange.getMessage().setBody(WebhookAckResponse.accepted(correlationId));
    }

    /**
     * Resolves a correlationId using the following priority order:
     * <ol>
     *   <li>{@code X-Correlation-Id} request header (set by gateway)</li>
     *   <li>{@code body.header.correlationId} field in the vendor payload</li>
     *   <li>{@code body.vendorReference} field in the vendor payload</li>
     *   <li>Random UUID (fallback)</li>
     * </ol>
     */
    private String resolveCorrelationId(Exchange exchange, Object body) {
        String headerVal = exchange.getMessage().getHeader("X-Correlation-Id", String.class);
        if (headerVal != null && !headerVal.isBlank()) {
            return headerVal;
        }
        if (body instanceof Map<?, ?> map) {
            Object header = map.get("header");
            if (header instanceof Map<?, ?> h) {
                Object corrId = h.get("correlationId");
                if (corrId != null && !String.valueOf(corrId).isBlank()) {
                    return String.valueOf(corrId);
                }
            }
            Object ref = map.get("vendorReference");
            if (ref != null && !String.valueOf(ref).isBlank()) {
                return String.valueOf(ref);
            }
        }
        String generated = UUID.randomUUID().toString();
        log.debug("No correlationId found in headers or payload – generated: {}", generated);
        return generated;
    }
}
