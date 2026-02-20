package com.example.transformation.processor;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.config.CartridgeResolver;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("validate")
public class ValidateProcessor implements Processor {
  private final CartridgeResolver cartridgeResolver;
  private final ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService;
  private final boolean persistenceEnabled;

  public ValidateProcessor(
      CartridgeResolver cartridgeResolver,
      ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService,
      @Value("${app.persistence.enabled:false}") boolean persistenceEnabled
  ) {
    this.cartridgeResolver = cartridgeResolver;
    this.persistenceService = persistenceService;
    this.persistenceEnabled = persistenceEnabled;
  }

  @Override
  public void process(Exchange exchange) {
    Object body = exchange.getMessage().getBody();
    if (!(body instanceof Map<?, ?>)) {
      throw new CartridgeException(ErrorCodes.code(ErrorCodes.REQUEST_BODY_TYPE), CartridgeException.ErrorType.FUNCTIONAL,
          "Expected JSON body parsed to Map/List but got: " + (body == null ? "null" : body.getClass()), null,
          "VALIDATION");
    }

    String cartridgeId = exchange.getMessage().getHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, String.class);
    if (cartridgeId == null || cartridgeId.isBlank()) {
      throw new CartridgeException(ErrorCodes.code(ErrorCodes.REQUEST_CARTRIDGE_ID_MISSING), CartridgeException.ErrorType.FUNCTIONAL,
          "Missing cartridge id header: " + ExchangeKeys.CARTRIDGE_ID_HEADER, null, "VALIDATION");
    }

    String currency = exchange.getMessage().getHeader(ExchangeKeys.CURRENCY_HEADER, String.class);
    String templateId = exchange.getMessage().getHeader(ExchangeKeys.TEMPLATE_HEADER, String.class);
    String flowId = exchange.getMessage().getHeader(ExchangeKeys.FLOW_ID_HEADER, String.class);
    String direction = exchange.getMessage().getHeader(ExchangeKeys.DIRECTION_HEADER, "outbound", String.class);

    // Resolve cartridge context using the lookup chain
    ResolvedCartridgeContext context = (flowId != null && !flowId.isBlank())
        ? cartridgeResolver.resolveWithFlowId(cartridgeId, currency, templateId, flowId)
        : cartridgeResolver.resolve(cartridgeId, currency, templateId, direction);
    exchange.setProperty(ExchangeKeys.RESOLVED_CONTEXT_PROP, context);

    String correlationId = exchange.getMessage().getHeader("X-Request-Id", String.class);
    if (persistenceEnabled && correlationId != null && !correlationId.isBlank()) {
      var svc = persistenceService.getIfAvailable();
      if (svc != null) {
        svc.markStep(correlationId, "VALIDATED", "VALIDATED");
        // Single-request flow only
      }
    }
  }
}

