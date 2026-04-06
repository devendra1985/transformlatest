package com.example.transformation.enrich;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * High-performance enrichment config loader with caching.
 */
public class EnrichmentLoader {

    private static final Optional<EnrichmentConfig> ABSENT = Optional.empty();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    private final ResourceLoader resourceLoader;
    private final ConcurrentMap<String, Optional<EnrichmentConfig>> cache = new ConcurrentHashMap<>(32);

    public EnrichmentLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Optional<EnrichmentConfig> loadOptional(String enrichResourcePath) {
        return cache.computeIfAbsent(enrichResourcePath, this::readYamlOptional);
    }

    private Optional<EnrichmentConfig> readYamlOptional(String enrichResourcePath) {
        Resource resource = resourceLoader.getResource(enrichResourcePath);
        if (!resource.exists()) {
            return ABSENT;
        }

        try (InputStream is = resource.getInputStream()) {
            EnrichmentConfig cfg = YAML_MAPPER.readValue(is, EnrichmentConfig.class);
            return cfg != null ? Optional.of(cfg) : ABSENT;
        } catch (Exception e) {
            throw new CartridgeException(
                    ErrorCodes.code(ErrorCodes.ENRICH_READ_FAILED),
                    CartridgeException.ErrorType.TECHNICAL,
                    "Failed to read enrichment YAML: " + enrichResourcePath,
                    e, null, "ENRICHMENT");
        }
    }

    public void clearCache() {
        cache.clear();
    }

    public int cacheSize() {
        return cache.size();
    }
}
