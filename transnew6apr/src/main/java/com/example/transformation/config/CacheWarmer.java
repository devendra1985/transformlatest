package com.example.transformation.config;

import com.example.transformation.enrich.EnrichmentLoader;
import com.example.transformation.routing.CartridgeBundle;
import com.example.transformation.routing.CartridgeBundleLoader;
import com.example.transformation.validation.DroolsValidationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Pre-warms caches at startup to avoid cold-start latency on first requests.
 * 
 * This ensures:
 * - All cartridge contexts are resolved and cached
 * - All enrichment configs are loaded and cached
 * - Template existence checks are cached
 */
@Component
public class CacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);

    private final CartridgeResolver cartridgeResolver;
    private final EnrichmentLoader enrichmentLoader;
    private final DroolsValidationService droolsValidationService;
    private final CartridgeBundleLoader bundleLoader;
    private final ConfigLoader configLoader;
    private final boolean warmCaches;
    private final boolean preloadRules;
    public CacheWarmer(
            CartridgeResolver cartridgeResolver,
            EnrichmentLoader enrichmentLoader,
            DroolsValidationService droolsValidationService,
            CartridgeBundleLoader bundleLoader,
            ConfigLoader configLoader,
            @Value("${app.cache.warm-on-startup:true}") boolean warmCaches,
            @Value("${app.cache.preload-rules:true}") boolean preloadRules) {
        this.cartridgeResolver = cartridgeResolver;
        this.enrichmentLoader = enrichmentLoader;
        this.droolsValidationService = droolsValidationService;
        this.bundleLoader = bundleLoader;
        this.configLoader = configLoader;
        this.warmCaches = warmCaches;
        this.preloadRules = preloadRules;
    }

    @PostConstruct
    public void warmCaches() {
        // Always clear caches first to ensure fresh data
        enrichmentLoader.clearCache();
        cartridgeResolver.clearCache();
        log.info("Cleared all caches");

        if (!warmCaches && !preloadRules) {
            log.info("Cache warming disabled");
            return;
        }

        log.info("Warming caches...");
        long start = System.currentTimeMillis();

        int contextsWarmed = 0;
        int enrichmentsWarmed = 0;
        int ruleBasesWarmed = 0;

        // Warm explicit bundles from cartridge.json for max performance
        java.util.Set<String> flowIds = new java.util.HashSet<>(
                configLoader.getCartridgeMasterConfig().cartridgeCodeFlows().values());
        if (flowIds.isEmpty()) {
            log.warn("No cartridgeCodeFlows configured; skipping cache warming");
            return;
        }
        try {
            java.util.Map<String, CartridgeBundle> bundles = bundleLoader.getAllBundles();
            for (CartridgeBundle bundle : bundles.values()) {
                for (String flowId : flowIds) {
                    try {
                        var context = cartridgeResolver.resolveWithFlowId(
                            bundle.getCartridgeId(), null, bundle.getTemplateId(), flowId);
                        if (warmCaches) {
                            contextsWarmed++;
                            enrichmentLoader.loadOptional(context.enrichPath());
                            enrichmentsWarmed++;
                        }
                        if (preloadRules) {
                            droolsValidationService.preload(context);
                            ruleBasesWarmed++;
                        }
                    } catch (Exception e) {
                        log.debug("Skipping bundle {} {}: {}", bundle.getTemplateId(), flowId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to preload cartridge bundles: {}", e.getMessage());
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("Cache warming completed in {}ms: {} contexts, {} enrichments, {} rule bases",
                elapsed, contextsWarmed, enrichmentsWarmed, ruleBasesWarmed);
    }
}
