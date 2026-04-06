package com.example.transformation.web;

import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.engine.CartridgeMapper;
import com.example.transformation.engine.CartridgeMapperRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standalone REST endpoint for Volante cartridge transformation.
 *
 * <p>Accepts an enriched CanonicalPayment (CJSON) and transforms it
 * into a vendor API DTO using the generated cartridge mapper identified
 * by the route key.
 *
 * <pre>
 *   POST /api/cartridge/{routeKey}
 *   Body: enriched CanonicalPayment JSON
 *   Response: vendor API DTO JSON
 * </pre>
 */
@RestController
@RequestMapping("/api/cartridge")
public class CartridgeController {

    private static final Logger log = LoggerFactory.getLogger(CartridgeController.class);

    private final CartridgeMapperRegistry registry;
    private final ObjectMapper objectMapper;

    public CartridgeController(CartridgeMapperRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    /**
     * Transform enriched CJSON into vendor API DTO.
     *
     * @param routeKey cartridge route key, e.g. {@code CREATE_PAYMENT_B2B}
     * @param body     enriched CanonicalPayment as JSON
     * @return vendor API DTO as JSON
     */
    @PostMapping(value = "/{routeKey}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transform(
            @PathVariable String routeKey,
            @RequestBody String body) throws Exception {

        log.info("Cartridge transform request: routeKey={}", routeKey);

        CanonicalPayment canonical = objectMapper.readValue(body, CanonicalPayment.class);

        CartridgeMapper<?> mapper = registry.get(routeKey);
        Object vendorApiDto = mapper.map(canonical);

        log.info("Cartridge transform complete: routeKey={}, outputType={}",
                routeKey, vendorApiDto.getClass().getSimpleName());

        return ResponseEntity.ok(vendorApiDto);
    }

    @GetMapping("/keys")
    public ResponseEntity<?> listKeys() {
        return ResponseEntity.ok(registry.availableKeys());
    }
}
