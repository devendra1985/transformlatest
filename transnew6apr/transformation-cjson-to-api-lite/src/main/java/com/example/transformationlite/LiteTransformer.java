package com.example.transformationlite;

import com.example.transformation.config.ConfigLoader;
import com.example.transformation.config.TemplateSelectionProperties;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.cjson.CjsonPayoutRequest;
import com.example.transformation.dto.visa.AccountPayoutRequest2;
import com.example.transformation.enrich.EnrichmentConfig;
import com.example.transformation.enrich.EnrichmentEngine;
import com.example.transformation.enrich.EnrichmentLoader;
import com.example.transformation.enrich.PaymentEnrichmentFunctions;
import com.example.transformation.mapper.CanonicalAccountPayoutMapper;
import com.example.transformation.normalize.CanonicalNormalizer;
import com.example.transformation.routing.CartridgeBundle;
import com.example.transformation.routing.CartridgeBundleLoader;
import com.example.transformation.routing.RoutingKey;
import com.example.transformation.routing.RoutingKeyResolver;
import com.example.transformation.validation.DroolsValidationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.mapstruct.factory.Mappers;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Lightweight CJSON -> API transformer with no HTTP or Camel.
 */
public class LiteTransformer {
  private static final String DEFAULT_CONFIG_BASE = "classpath:config";
  private static final String DEFAULT_CARTRIDGE_BASE = "classpath:cartridges";

  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private final ConfigLoader configLoader;
  private final RoutingKeyResolver routingKeyResolver;
  private final CartridgeBundleLoader bundleLoader;
  private final com.example.transformation.config.CartridgeResolver cartridgeResolver;
  private final EnrichmentLoader enrichmentLoader;
  private final EnrichmentEngine enrichmentEngine;
  private final DroolsValidationService validationService;
  private final CanonicalNormalizer canonicalNormalizer;
  private final CanonicalAccountPayoutMapper mapper;
  private final String routingKeyFormat;
  private final GenericApplicationContext appContext;

  public LiteTransformer() {
    this.objectMapper = new ObjectMapper();
    this.resourceLoader = new DefaultResourceLoader(LiteTransformer.class.getClassLoader());
    this.configLoader = new ConfigLoader(resourceLoader, DEFAULT_CONFIG_BASE);
    this.configLoader.init();

    TemplateSelectionProperties templateSelectionProperties = loadTemplateSelectionProperties();
    this.routingKeyResolver = new RoutingKeyResolver(templateSelectionProperties, configLoader);
    this.bundleLoader = new CartridgeBundleLoader(objectMapper, resourceLoader);
    this.cartridgeResolver = new com.example.transformation.config.CartridgeResolver(
        configLoader, resourceLoader, DEFAULT_CARTRIDGE_BASE);
    this.cartridgeResolver.init();

    this.enrichmentLoader = new EnrichmentLoader(resourceLoader);
    this.enrichmentEngine = new EnrichmentEngine();
    this.validationService = new DroolsValidationService();
    this.canonicalNormalizer = new CanonicalNormalizer();
    this.mapper = Mappers.getMapper(CanonicalAccountPayoutMapper.class);
    this.routingKeyFormat = loadRoutingKeyFormat();

    this.appContext = new GenericApplicationContext();
    this.appContext.registerBean(PaymentEnrichmentFunctions.class);
    this.appContext.refresh();
  }

  /**
   * Transforms CJSON into AccountPayoutRequest2.
   */
  public AccountPayoutRequest2 transform(String cjson) {
    Map<String, Object> input = parseJson(cjson);

    CjsonPayoutRequest request = objectMapper.convertValue(input, CjsonPayoutRequest.class);
    CanonicalPayment canonical = canonicalNormalizer.normalize(request);
    RoutingKey routingKey = routingKeyResolver.resolve(canonical);

    var selection = configLoader.getCartridgeMasterConfig().resolveSelection(
        routingKey.getCartridgeCode(),
        canonical.getTransaction() != null ? canonical.getTransaction().getApiType() : null,
        canonical.getTransaction() != null ? canonical.getTransaction().getSchemaType() : null);
    if (selection == null) {
      throw new IllegalStateException("No cartridge selection configured for cartridgeCode/apiType/schemaType");
    }

    String key = routingKey.asKey(routingKeyFormat);
    CartridgeBundle bundle = bundleLoader.getBundle(key);
    if (bundle == null) {
      throw new IllegalStateException("Cartridge bundle not found for routing key: " + key);
    }

    ResolvedCartridgeContext context = cartridgeResolver.resolveWithFlowId(
        selection.cartridgeId(),
        canonical.getTransaction() != null ? canonical.getTransaction().getCurrency() : null,
        bundle.getTemplateId(),
        selection.flowId());

    Map<String, Object> enriched = applyEnrichment(input, context);

    CjsonPayoutRequest enrichedRequest = toCjsonRequest(enriched);
    CanonicalPayment enrichedCanonical = canonicalNormalizer.normalize(enrichedRequest);
    AccountPayoutRequest2 apiRequest = mapper.toAccount(enrichedCanonical);
    validationService.validate(apiRequest, context);
    return apiRequest;
  }

  private Map<String, Object> parseJson(String cjson) {
    try {
      return objectMapper.readValue(cjson, new TypeReference<>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse CJSON", e);
    }
  }

  private Map<String, Object> applyEnrichment(Map<String, Object> input, ResolvedCartridgeContext context) {
    if (context == null || context.enrichPath() == null) {
      return new LinkedHashMap<>(input);
    }
    Optional<EnrichmentConfig> cfg = enrichmentLoader.loadOptional(context.enrichPath());
    if (cfg.isEmpty()) {
      return new LinkedHashMap<>(input);
    }
    return enrichmentEngine.apply(new LinkedHashMap<>(input), cfg.get(), appContext);
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
      paymentData.setTxInf(java.util.List.of(txInf));
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

  private TemplateSelectionProperties loadTemplateSelectionProperties() {
    Map<String, Object> app = loadYaml("classpath:application.yaml");
    Object appNode = app.get("app");
    if (!(appNode instanceof Map<?, ?> appMap)) {
      throw new IllegalStateException("Missing app template-selection in application.yaml");
    }
    Object templateSelection = appMap.get("template-selection");
    if (!(templateSelection instanceof Map<?, ?> sel)) {
      throw new IllegalStateException("Missing app.template-selection in application.yaml");
    }
    TemplateSelectionProperties props = new TemplateSelectionProperties();
    props.setRecipientTypeBusiness(String.valueOf(sel.get("recipient-type-business")));
    props.setRecipientTypeIndividual(String.valueOf(sel.get("recipient-type-individual")));
    props.setSegmentBusiness(String.valueOf(sel.get("segment-business")));
    props.setSegmentIndividual(String.valueOf(sel.get("segment-individual")));
    props.setTemplateIdFormat(String.valueOf(sel.get("template-id-format")));
    return props;
  }

  private String loadRoutingKeyFormat() {
    Map<String, Object> app = loadYaml("classpath:application.yaml");
    Object appNode = app.get("app");
    if (!(appNode instanceof Map<?, ?> appMap)) {
      throw new IllegalStateException("Missing app routing in application.yaml");
    }
    Object routing = appMap.get("routing");
    if (!(routing instanceof Map<?, ?> routingMap)) {
      throw new IllegalStateException("Missing app.routing in application.yaml");
    }
    Object format = routingMap.get("key-format");
    if (format == null) {
      throw new IllegalStateException("Missing app.routing.key-format in application.yaml");
    }
    return String.valueOf(format);
  }

  private Map<String, Object> loadYaml(String path) {
    Resource resource = resourceLoader.getResource(path);
    if (!resource.exists()) {
      throw new IllegalStateException("Missing resource: " + path);
    }
    try (InputStream is = resource.getInputStream()) {
      ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
      return yaml.readValue(is, new TypeReference<>() {});
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read YAML: " + path, e);
    }
  }
}
