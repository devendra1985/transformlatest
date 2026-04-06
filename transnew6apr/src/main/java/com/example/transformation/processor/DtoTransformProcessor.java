package com.example.transformation.processor;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.engine.CartridgeMapper;
import com.example.transformation.engine.CartridgeMapperRegistry;
import com.example.transformation.routing.RoutingKey;
import com.example.transformation.validation.DroolsValidationService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Transforms CanonicalPayment to vendor API DTO using generated cartridge mappers.
 *
 * <p>Volante-style: the mapping is compiled at build time into direct getter/setter
 * calls — zero reflection, zero ObjectMapper conversion at runtime.
 *
 * <p>Contract:
 * <pre>
 *   Input : CanonicalPayment (on exchange, set by RoutingProcessor)
 *   Engine: Generated CartridgeMapper (looked up via CartridgeMapperRegistry)
 *   Output: vendor API DTO (e.g. AccountPayoutRequest2, CancelPayoutRequest, etc.)
 * </pre>
 */
@Component("dtoTransform")
public class DtoTransformProcessor implements Processor {

    private final CartridgeMapperRegistry registry;
    private final DroolsValidationService validationService;
    private final ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService;
    private final boolean persistenceEnabled;

    public DtoTransformProcessor(
        CartridgeMapperRegistry registry,
        DroolsValidationService validationService,
        ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService,
        @Value("${app.persistence.enabled:false}") boolean persistenceEnabled) {
        this.registry = registry;
        this.validationService = validationService;
        this.persistenceService = persistenceService;
        this.persistenceEnabled = persistenceEnabled;
    }

    @Override
    public void process(Exchange exchange) {
        CanonicalPayment canonical = exchange.getProperty(
            ExchangeKeys.CANONICAL_PAYMENT_PROP, CanonicalPayment.class);
        ResolvedCartridgeContext context = exchange.getProperty(
            ExchangeKeys.RESOLVED_CONTEXT_PROP, ResolvedCartridgeContext.class);

        if (canonical == null) {
            throw new CartridgeException(
                ErrorCodes.code(ErrorCodes.REQUEST_BODY_TYPE),
                CartridgeException.ErrorType.FUNCTIONAL,
                "CanonicalPayment not found on exchange — RoutingProcessor must run first",
                null, "TRANSFORM");
        }

        String apiType = canonical.getTransaction() != null
            ? canonical.getTransaction().getApiType() : null;
        if (apiType == null || apiType.isBlank()) {
            throw new CartridgeException(
                ErrorCodes.code(ErrorCodes.REQUEST_BODY_TYPE),
                CartridgeException.ErrorType.FUNCTIONAL,
                "apiType is missing in CanonicalPayment", null, "TRANSFORM");
        }

        RoutingKey routingKey = exchange.getProperty(ExchangeKeys.ROUTING_KEY_PROP, RoutingKey.class);
        String segment = routingKey != null ? routingKey.getSegment() : null;

        CartridgeMapper<?> mapper = registry.get(apiType, segment);
        Object apiDto = mapper.map(canonical);

        validationService.validate(apiDto, context);

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(apiDto);

        markPersisted(exchange);
    }

    private void markPersisted(Exchange exchange) {
        String correlationId = exchange.getMessage().getHeader("X-Request-Id", String.class);
        if (persistenceEnabled && correlationId != null && !correlationId.isBlank()) {
            var svc = persistenceService.getIfAvailable();
            if (svc != null) svc.markStep(correlationId, "TRANSFORMED", "TRANSFORMED");
        }
    }
}
