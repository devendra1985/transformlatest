package com.example.transformation.processor;

import com.example.transformation.config.model.ResolvedCartridgeContext;
import com.example.transformation.enrich.EnrichmentConfig;
import com.example.transformation.enrich.EnrichmentEngine;
import com.example.transformation.enrich.EnrichmentLoader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Enrichment / enhancement hook.
 *
 * Keep this intentionally simple:
 * - normalize currency to uppercase (if present)
 * - trim bic (if present)
 *
 * Add any derived/default fields here before mapping.
 */
@Component("enrich")
public class EnrichProcessor implements Processor {
  private final EnrichmentLoader enrichmentLoader;
  private final EnrichmentEngine engine;
  private final ApplicationContext appContext;
  private final ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService;
  private final boolean persistenceEnabled;

  public EnrichProcessor(
      EnrichmentLoader enrichmentLoader,
      EnrichmentEngine engine,
      ApplicationContext appContext,
      ObjectProvider<com.example.transformation.persistence.PayloadPersistenceService> persistenceService,
      @Value("${app.persistence.enabled:false}") boolean persistenceEnabled
  ) {
    this.enrichmentLoader = enrichmentLoader;
    this.engine = engine;
    this.appContext = appContext;
    this.persistenceService = persistenceService;
    this.persistenceEnabled = persistenceEnabled;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void process(Exchange exchange) {
    // Get resolved context from validate processor
    ResolvedCartridgeContext context = exchange.getProperty(
        ExchangeKeys.RESOLVED_CONTEXT_PROP, ResolvedCartridgeContext.class);

    Object body = exchange.getMessage().getBody();
    if (!(body instanceof Map<?, ?> m)) {
      return;
    }

    Map<String, Object> copy = new LinkedHashMap<>((Map<String, Object>) m);

    // Cartridge-specific enrichment rules using resolved context
    copy = applyEnrichment(context, copy);

    exchange.getMessage().setBody(copy);

    String correlationId = exchange.getMessage().getHeader("X-Request-Id", String.class);
    if (persistenceEnabled && correlationId != null && !correlationId.isBlank()) {
      var svc = persistenceService.getIfAvailable();
      if (svc != null) svc.markStep(correlationId, "ENRICHED", "ENRICHED");
    }
  }

  private Map<String, Object> applyEnrichment(ResolvedCartridgeContext context, Map<String, Object> input) {
    Map<String, Object> copy = new LinkedHashMap<>(input);

    // Cartridge-specific enrichment rules using resolved enrich path
    if (context != null && context.enrichPath() != null) {
      Optional<EnrichmentConfig> cfg = enrichmentLoader.loadOptional(context.enrichPath());
      if (cfg.isPresent()) {
        copy = engine.apply(copy, cfg.get(), appContext);
      }
    }

    Object currency = copy.get("currency");
    if (currency instanceof String s && !s.isBlank()) {
      copy.put("currency", s.trim().toUpperCase());
    }

    Object bic = copy.get("bic");
    if (bic instanceof String s && !s.isBlank()) {
      copy.put("bic", s.trim());
    }

    return copy;
  }
}

