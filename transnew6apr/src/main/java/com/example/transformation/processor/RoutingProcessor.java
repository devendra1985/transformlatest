package com.example.transformation.processor;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.config.ConfigLoader;
import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.cjson.CjsonPayoutRequest;
import com.example.transformation.fieldset.FieldSetResolver;
import com.example.transformation.normalize.CanonicalNormalizer;
import com.example.transformation.routing.CartridgeBundle;
import com.example.transformation.routing.CartridgeBundleLoader;
import com.example.transformation.routing.RoutingKey;
import com.example.transformation.routing.RoutingKeyResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("routeResolve")
public class RoutingProcessor implements Processor {
  private static final Logger LOG = LoggerFactory.getLogger(RoutingProcessor.class);
  private final ObjectMapper objectMapper;
  private final CanonicalNormalizer canonicalNormalizer;
  private final RoutingKeyResolver routingKeyResolver;
  private final CartridgeBundleLoader bundleLoader;
  private final String routingKeyFormat;
  private final ConfigLoader configLoader;
  private final FieldSetResolver fieldSetResolver;

  public RoutingProcessor(
      ObjectMapper objectMapper,
      CanonicalNormalizer canonicalNormalizer,
      RoutingKeyResolver routingKeyResolver,
      CartridgeBundleLoader bundleLoader,
      ConfigLoader configLoader,
      FieldSetResolver fieldSetResolver,
      @Value("${app.routing.key-format}") String routingKeyFormat) {
    this.objectMapper = objectMapper;
    this.canonicalNormalizer = canonicalNormalizer;
    this.routingKeyResolver = routingKeyResolver;
    this.bundleLoader = bundleLoader;
    this.configLoader = configLoader;
    this.fieldSetResolver = fieldSetResolver;
    this.routingKeyFormat = routingKeyFormat;
  }

  @Override
  public void process(Exchange exchange) {
    Object body = exchange.getMessage().getBody();
    if (!(body instanceof Map<?, ?>)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) body;
    CjsonPayoutRequest request = objectMapper.convertValue(map, CjsonPayoutRequest.class);
    CanonicalPayment canonical = canonicalNormalizer.normalize(request);
    RoutingKey routingKey = routingKeyResolver.resolve(canonical);

    var selection = configLoader.getCartridgeMasterConfig().resolveSelection(
        routingKey.getCartridgeCode(),
        canonical.getTransaction() != null ? canonical.getTransaction().getApiType() : null,
        canonical.getTransaction() != null ? canonical.getTransaction().getSchemaType() : null);
    if (selection == null) {
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.CARTRIDGE_FLOW_NOT_FOUND),
          CartridgeException.ErrorType.FUNCTIONAL,
          "No cartridge selection configured for cartridgeCode/apiType/schemaType",
          null,
          "ROUTE");
    }
    exchange.getMessage().setHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, selection.cartridgeId());
    exchange.getMessage().setHeader(ExchangeKeys.FLOW_ID_HEADER, selection.flowId());

    String key = routingKey.asKey(routingKeyFormat);
    CartridgeBundle bundle = bundleLoader.getBundle(key);
    if (bundle == null) {
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.CARTRIDGE_NOT_FOUND),
          CartridgeException.ErrorType.FUNCTIONAL,
          "Cartridge bundle not found for routing key: " + key,
          null,
          "ROUTE");
    }

    exchange.getMessage().setHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, bundle.getCartridgeId());
    exchange.getMessage().setHeader(ExchangeKeys.TEMPLATE_HEADER, bundle.getTemplateId());
    exchange.setProperty(ExchangeKeys.CARTRIDGE_BUNDLE_PROP, bundle);
    LOG.info("Routing key resolved: {} -> template {}", key, bundle.getTemplateId());

    if (canonical.getTransaction() != null) {
      exchange.getMessage().setHeader(ExchangeKeys.CURRENCY_HEADER, canonical.getTransaction().getCurrency());
    }

    Set<String> requiredFields = fieldSetResolver.resolve(routingKey);
    exchange.setProperty(ExchangeKeys.REQUIRED_FIELD_SET_PROP, requiredFields);
    exchange.setProperty(ExchangeKeys.ROUTING_KEY_PROP, routingKey);
    exchange.setProperty(ExchangeKeys.CANONICAL_PAYMENT_PROP, canonical);
    LOG.info("Field set resolved for {}/{}/{}: {} fields",
        routingKey.getCountry(), routingKey.getCurrency(), routingKey.getSegment(),
        requiredFields.size());
  }
}
