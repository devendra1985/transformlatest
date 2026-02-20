package com.example.transformation.processor;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.dto.cjson.CjsonPayoutRequest;
import com.example.transformation.mapper.CanonicalAccountPayoutMapper;
import com.example.transformation.normalize.CanonicalNormalizer;
import com.example.transformation.validation.DroolsValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("dtoTransform")
public class DtoTransformProcessor implements Processor {
  private final ObjectMapper objectMapper;
  private final CanonicalNormalizer canonicalNormalizer;
  private final CanonicalAccountPayoutMapper canonicalAccountPayoutMapper;
  private final DroolsValidationService validationService;
  private final ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService;
  private final boolean persistenceEnabled;

  public DtoTransformProcessor(
      ObjectMapper objectMapper,
      CanonicalNormalizer canonicalNormalizer,
      CanonicalAccountPayoutMapper canonicalAccountPayoutMapper,
      DroolsValidationService validationService,
      ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService,
      @Value("${app.persistence.enabled:false}") boolean persistenceEnabled) {
    this.objectMapper = objectMapper;
    this.canonicalNormalizer = canonicalNormalizer;
    this.canonicalAccountPayoutMapper = canonicalAccountPayoutMapper;
    this.validationService = validationService;
    this.persistenceService = persistenceService;
    this.persistenceEnabled = persistenceEnabled;
  }

  @Override
  public void process(Exchange exchange) {
    Object input = exchange.getMessage().getBody();
    com.example.transformation.config.model.ResolvedCartridgeContext context =
        exchange.getProperty(ExchangeKeys.RESOLVED_CONTEXT_PROP,
            com.example.transformation.config.model.ResolvedCartridgeContext.class);
    if (!(input instanceof Map<?, ?> map)) {
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.REQUEST_BODY_TYPE),
          CartridgeException.ErrorType.FUNCTIONAL,
          "Expected JSON object for DTO transform but got: " + (input == null ? "null" : input.getClass()),
          null,
          "TRANSFORM");
    }

    @SuppressWarnings("unchecked")
    Object result = mapToDto((Map<String, Object>) map, context);
    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
    exchange.getMessage().setBody(result);

    markPersisted(exchange);
  }

  private Object mapToDto(
      Map<String, Object> input,
      com.example.transformation.config.model.ResolvedCartridgeContext context) {
    CjsonPayoutRequest request = toCjsonRequest(input);
    var canonical = canonicalNormalizer.normalize(request);
    Object apiRequest = canonicalAccountPayoutMapper.toAccount(canonical);
    validationService.validate(apiRequest, context);
    return apiRequest;
  }

  private CjsonPayoutRequest toCjsonRequest(Map<String, Object> input) {
    CjsonPayoutRequest request = objectMapper.convertValue(input, CjsonPayoutRequest.class);
    boolean hasTxInf = request.getPaymentData() != null
        && request.getPaymentData().getTxInf() != null
        && !request.getPaymentData().getTxInf().isEmpty();

    CjsonPayoutRequest.TxInf txInf = objectMapper.convertValue(input, CjsonPayoutRequest.TxInf.class);
    if (!hasTxInf) {
      CjsonPayoutRequest.PaymentData paymentData = request.getPaymentData();
      if (paymentData == null) {
        paymentData = new CjsonPayoutRequest.PaymentData();
        request.setPaymentData(paymentData);
      }
      paymentData.setTxInf(List.of(txInf));
    }

    if (request.getHeader() == null) {
      Object header = input.get("header");
      if (header instanceof Map<?, ?>) {
        CjsonPayoutRequest.Header hdr = objectMapper.convertValue(header, CjsonPayoutRequest.Header.class);
        request.setHeader(hdr);
      }
    }

    return request;
  }

  private void markPersisted(Exchange exchange) {
    String correlationId = exchange.getMessage().getHeader("X-Request-Id", String.class);
    if (persistenceEnabled && correlationId != null && !correlationId.isBlank()) {
      var svc = persistenceService.getIfAvailable();
      if (svc != null) svc.markStep(correlationId, "TRANSFORMED", "TRANSFORMED");
    }
  }
}
