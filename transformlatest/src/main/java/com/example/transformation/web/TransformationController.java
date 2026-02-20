package com.example.transformation.web;

import com.example.transformation.config.CartridgeResolver;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import com.example.transformation.processor.ExchangeKeys;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for CJSON to API transformations.
 * 
 * Endpoints:
 * - POST /api/transform/{cartridgeId} - Single request
 * 
 * Headers:
 * - X-Currency (optional): Currency code for currency-specific templates (USD, EUR, INR)
 * - X-Template (optional): Explicit template id override (ex: VISABA_USA_USD_B2B_TEMPLATE)
 */
@RestController
@RequestMapping("/api/transform")
public class TransformationController {

    private static final Logger log = LoggerFactory.getLogger(TransformationController.class);

    private final ProducerTemplate producerTemplate;
    private final CartridgeResolver cartridgeResolver;
    public TransformationController(
            ProducerTemplate producerTemplate,
            CartridgeResolver cartridgeResolver) {
        this.producerTemplate = producerTemplate;
        this.cartridgeResolver = cartridgeResolver;
    }

    /**
     * Single request transformation.
     * POST /api/transform/{cartridgeId}
     */
    @PostMapping(value = "/{cartridgeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transform(
            @PathVariable String cartridgeId,
            @RequestHeader(value = "X-Currency", required = false) String currency,
            @RequestHeader(value = "X-Template", required = false) String templateId,
            @RequestBody Map<String, Object> body) {

      // Propagate request id to servlet response as well (useful for tracing + persistence updates)
      String requestId = null;
      Object header = body.get("header");
      if (header instanceof Map<?, ?> h) {
          Object corrId = h.get("correlationId");
          if (corrId != null && !String.valueOf(corrId).isBlank()) {
              requestId = String.valueOf(corrId);
          }
      }
      if (requestId != null) {
          // store in MDC + response header is handled by RawPersistProcessor via exchange header
          // but also set on servlet response below
      }

        ResolvedCartridgeContext context = cartridgeResolver.resolve(cartridgeId, currency, templateId, "outbound");
        log.info("Resolved context: endpoint={}", context.directEndpoint());

        Exchange out = producerTemplate.request(context.directEndpoint(), e -> {
            e.getMessage().setBody(body);
            e.getMessage().setHeader(ExchangeKeys.CARTRIDGE_ID_HEADER, cartridgeId);
            e.getMessage().setHeader(ExchangeKeys.CURRENCY_HEADER, currency);
            e.getMessage().setHeader(ExchangeKeys.TEMPLATE_HEADER, templateId);
        });

        // Check for exceptions in the exchange
        if (out.getException() != null) {
            log.error("Exchange exception: ", out.getException());
            throw new RuntimeException("Transformation failed", out.getException());
        }

        log.info("Transformation complete, response body type: {}", 
                out.getMessage().getBody() != null ? out.getMessage().getBody().getClass().getSimpleName() : "null");

      ResponseEntity<?> resp = buildResponse(out);
      String rid = out.getMessage().getHeader("X-Request-Id", String.class);
      if (rid != null && !rid.isBlank()) {
          return ResponseEntity.status(resp.getStatusCode())
                  .headers(resp.getHeaders())
                  .header("X-Request-Id", rid)
                  .body(resp.getBody());
      }
      return resp;
    }

    private ResponseEntity<?> buildResponse(Exchange out) {
        Object responseBody = out.getMessage().getBody();
        String contentType = out.getMessage().getHeader(Exchange.CONTENT_TYPE, String.class);
        MediaType mt = (contentType == null || contentType.isBlank()) 
                ? MediaType.APPLICATION_JSON 
                : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok().contentType(mt).body(responseBody);
    }

}
