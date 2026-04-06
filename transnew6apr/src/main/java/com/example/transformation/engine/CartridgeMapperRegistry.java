package com.example.transformation.engine;

import com.example.transformation.dto.canonical.CanonicalPayment;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry of all generated {@link CartridgeMapper} implementations.
 *
 * <p>Spring auto-discovers every generated mapper (they are {@code @Component}s)
 * and this registry indexes them by template key for O(1) lookup at runtime.
 *
 * <p>At startup, performs a dry-run validation of every mapper to catch
 * any residual issues that survived build-time validation.
 */
@Component
public class CartridgeMapperRegistry {

    private static final Logger log = LoggerFactory.getLogger(CartridgeMapperRegistry.class);

    private final Map<String, CartridgeMapper<?>> mappers;

    public CartridgeMapperRegistry(List<CartridgeMapper<?>> allMappers) {
        this.mappers = allMappers.stream()
            .collect(Collectors.toMap(CartridgeMapper::templateKey, m -> m));
        log.info("CartridgeMapperRegistry loaded {} mappers: {}",
            mappers.size(), mappers.keySet());
    }

    @PostConstruct
    void startupValidation() {
        CanonicalPayment dummy = new CanonicalPayment();
        dummy.setTransaction(new CanonicalPayment.CanonicalTransaction());

        for (var entry : mappers.entrySet()) {
            try {
                entry.getValue().map(dummy);
                log.debug("Startup validation passed: {}", entry.getKey());
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Startup validation failed for mapper [" + entry.getKey() + "]: " + e.getMessage(), e);
            }
        }
        log.info("All {} cartridge mappers passed startup validation", mappers.size());
    }

    /**
     * Look up a mapper by API type and segment.
     * Key format: {@code API_TYPE_SEGMENT} (e.g. {@code CREATE_PAYMENT_B2B}).
     */
    public CartridgeMapper<?> get(String apiType, String segment) {
        String key = buildKey(apiType, segment);
        CartridgeMapper<?> mapper = mappers.get(key);
        if (mapper == null) {
            throw new IllegalArgumentException(
                "No cartridge mapper registered for key: " + key
                    + ". Available: " + mappers.keySet());
        }
        return mapper;
    }

    /**
     * Look up a mapper by a single route key (e.g. {@code "CREATE_PAYMENT_B2B"}).
     */
    public CartridgeMapper<?> get(String routeKey) {
        String key = routeKey.trim().toUpperCase();
        CartridgeMapper<?> mapper = mappers.get(key);
        if (mapper == null) {
            throw new IllegalArgumentException(
                "No cartridge mapper registered for key: " + key
                    + ". Available: " + mappers.keySet());
        }
        return mapper;
    }

    public Set<String> availableKeys() {
        return mappers.keySet();
    }

    public boolean contains(String apiType, String segment) {
        return mappers.containsKey(buildKey(apiType, segment));
    }

    private static String buildKey(String apiType, String segment) {
        String key = apiType.trim().toUpperCase();
        if (segment != null && !segment.isBlank()) {
            key = key + "_" + segment.trim().toUpperCase();
        }
        return key;
    }
}
