package com.example.transformation.config;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import com.example.transformation.config.model.TransformationFlowMasterConfig;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * High-performance cartridge resolver with aggressive caching.
 * 
 * Performance optimizations:
 * - Pre-built path cache at startup
 * - Interned cache keys to reduce GC pressure
 * - Single ConcurrentHashMap lookup per resolve
 * - No string concatenation in hot paths
 */
@Component
public class CartridgeResolver {

    private static final Logger log = LoggerFactory.getLogger(CartridgeResolver.class);

    private final ConfigLoader configLoader;
    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourcePatternResolver;
    private final String cartridgesBasePath;

    // Primary cache - fully resolved contexts (interned keys)
    private final Map<String, ResolvedCartridgeContext> contextCache = new ConcurrentHashMap<>(64);
    
    // Pre-built base paths: "provider/cartridgeId" -> basePath
    private final Map<String, String> basePathCache = new ConcurrentHashMap<>(32);
    
    // Template existence cache
    private final Map<String, Boolean> templateExistsCache = new ConcurrentHashMap<>(64);

    public CartridgeResolver(
            ConfigLoader configLoader,
            ResourceLoader resourceLoader,
            @Value("${app.cartridges.base-path:classpath:cartridges}") String cartridgesBasePath) {
        this.configLoader = configLoader;
        this.resourceLoader = resourceLoader;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        this.cartridgesBasePath = cartridgesBasePath;
    }

    @PostConstruct
    public void init() {
        // Pre-build base paths for all known cartridges
        preBuildBasePaths();
        log.info("CartridgeResolver initialized with {} pre-built paths", basePathCache.size());
    }

    private void preBuildBasePaths() {
        var schemas = configLoader.getSchemaMasterConfig().cartridges();
        for (var entry : schemas.entrySet()) {
            String cartridgeId = entry.getKey();
            String provider = entry.getValue().provider();
            String basePath = cartridgesBasePath + "/" + provider + "/" + cartridgeId;
            basePathCache.put(cartridgeId, basePath);
        }
    }

    /**
     * Resolves cartridge context with O(1) cache lookup.
     * Cache key is interned to reduce memory and enable identity comparison.
     */
    public ResolvedCartridgeContext resolve(
            String cartridgeId,
            String currency,
            String templateId,
            String direction) {
        throw new IllegalStateException("Direction-based resolution is disabled; use resolveWithFlowId instead.");
    }

    public ResolvedCartridgeContext resolveWithFlowId(
            String cartridgeId,
            String currency,
            String templateId,
            String flowId) {
        String cacheKey = cacheKey(cartridgeId, currency, templateId, flowId).intern();
        return contextCache.computeIfAbsent(cacheKey, k -> doResolveWithFlowId(cartridgeId, currency, templateId, flowId));
    }

    public ResolvedCartridgeContext resolve(String cartridgeId, String currency) {
        throw new IllegalStateException("Direction-based resolution is disabled; use resolveWithFlowId instead.");
    }

    public ResolvedCartridgeContext resolve(String cartridgeId, String currency, String direction) {
        throw new IllegalStateException("Direction-based resolution is disabled; use resolveWithFlowId instead.");
    }

    private ResolvedCartridgeContext doResolveWithFlowId(
            String cartridgeId,
            String currency,
            String templateId,
            String flowId) {
        String basePath = basePathCache.get(cartridgeId);
        if (basePath == null) {
            throw cartridgeNotFound(cartridgeId);
        }

        var schema = configLoader.getSchemaMasterConfig().cartridges().get(cartridgeId);
        var flowDef = getFlowDefinition(flowId);
        String templatePath = resolveTemplatePath(basePath, currency, templateId);

        return new ResolvedCartridgeContext(
                schema.provider(),
                cartridgeId,
                currency,
                templateId,
                flowId,
                flowId,
                schema.inputFormat(),
                flowDef.from(),
                flowDef.to(),
                templatePath + "/enrich.yaml",
                templatePath + "/route.yaml"
        );
    }

    private TransformationFlowMasterConfig.FlowDefinition getFlowDefinition(String flowId) {
        var flowDef = configLoader.getTransformationFlowMasterConfig().flows().get(flowId);
        if (flowDef == null) {
            throw flowDefinitionNotFound(flowId);
        }
        return flowDef;
    }

    private String resolveTemplatePath(String basePath, String currency, String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw templateNotFound(basePath + "/templates/<templateId>");
        }
        String templatePath = basePath + "/templates/" + templateId;
        if (!templateExists(templatePath)) {
            throw templateNotFound(templatePath);
        }
        return templatePath;
    }

    private boolean templateExists(String path) {
        return templateExistsCache.computeIfAbsent(path, p -> {
            Resource resource = resourceLoader.getResource(p + "/route.yaml");
            return resource.exists();
        });
    }

    // Efficient cache key builder - avoids StringBuilder for common cases
    private static String cacheKey(String cartridgeId, String currency, String templateId, String direction) {
        String curr = (currency == null) ? "" : currency;
        String tmpl = (templateId == null) ? "" : templateId;
        return cartridgeId + "|" + tmpl + "|" + curr + "|" + direction;
    }

    public boolean cartridgeExists(String cartridgeId) {
        return basePathCache.containsKey(cartridgeId);
    }

    public boolean currencyTemplateExists(String cartridgeId, String currency) {
        return false;
    }

    public boolean templateExists(String cartridgeId, String templateId) {
        String basePath = basePathCache.get(cartridgeId);
        return basePath != null && templateExists(basePath + "/templates/" + templateId);
    }

    public List<String> listTemplateIds(String cartridgeId) {
        String basePath = basePathCache.get(cartridgeId);
        if (basePath == null) {
            return List.of();
        }
        String pattern = toClasspathPattern(basePath + "/templates/*/route.yaml");
        List<String> templateIds = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            for (Resource resource : resources) {
                String path = toClassPath(resource);
                if (path == null) {
                    continue;
                }
                String[] parts = path.split("/");
                if (parts.length < 6) {
                    continue;
                }
                templateIds.add(parts[parts.length - 2]);
            }
        } catch (Exception e) {
            log.debug("Template discovery failed for {}: {}", cartridgeId, e.getMessage());
        }
        return templateIds;
    }

    public void clearCache() {
        contextCache.clear();
        templateExistsCache.clear();
        log.info("CartridgeResolver caches cleared");
    }

    private String toClasspathPattern(String path) {
        if (path.startsWith("classpath*:")) {
            return path;
        }
        if (path.startsWith("classpath:")) {
            return "classpath*:" + path.substring("classpath:".length());
        }
        return path;
    }

    private String toClassPath(Resource resource) {
        try {
            String path = resource.getURL().getPath();
            int idx = path.indexOf("/cartridges/");
            if (idx >= 0) {
                return path.substring(idx + 1);
            }
            return resource.getFilename();
        } catch (Exception e) {
            return resource.getFilename();
        }
    }

    // Pre-built exception factories to avoid string concatenation in error paths
    private CartridgeException cartridgeNotFound(String cartridgeId) {
        return new CartridgeException(
                ErrorCodes.code(ErrorCodes.CARTRIDGE_NOT_FOUND),
                CartridgeException.ErrorType.FUNCTIONAL,
                "Cartridge not found: " + cartridgeId, null, "RESOLVE");
    }

    private CartridgeException flowDefinitionNotFound(String flowId) {
        return new CartridgeException(
                ErrorCodes.code(ErrorCodes.CARTRIDGE_FLOW_NOT_FOUND),
                CartridgeException.ErrorType.FUNCTIONAL,
                "Flow definition not found: " + flowId, null, "RESOLVE");
    }

    private CartridgeException templateNotFound(String path) {
        return new CartridgeException(
                ErrorCodes.code(ErrorCodes.CARTRIDGE_TEMPLATE_NOT_FOUND),
                CartridgeException.ErrorType.FUNCTIONAL,
                "Cartridge templates not found: " + path, null, "RESOLVE");
    }
}
