package com.example.transformation.cartridge;

import com.example.transformation.config.ConfigLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Programmatically creates Camel routes for each cartridge and currency combination.
 * This ensures routes are properly registered before Camel context starts.
 */
@Component
public class CartridgeRouteBuilder extends RouteBuilder {

    private final ConfigLoader configLoader;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public CartridgeRouteBuilder(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public void configure() throws Exception {
        // Ensure base routes are created for known cartridges
        var cartridges = configLoader.getSchemaMasterConfig().cartridges();
        for (String cartridgeId : cartridges.keySet()) {
            createRoute(cartridgeId, null);
        }

        // Create routes for all discovered templates/currency variants
        Set<String> created = new HashSet<>();
        Resource[] routes = resolver.getResources("classpath*:cartridges/*/*/**/route.yaml");
        for (Resource resource : routes) {
            String path = toClassPath(resource);
            if (path == null) {
                continue;
            }
            String[] parts = path.split("/");
            if (parts.length < 4) {
                continue;
            }
            String cartridgeId = parts[2];
            String variantKey = variantKeyFrom(parts);
            if (variantKey == null) {
                continue;
            }
            String endpoint = cartridgeId + ":" + variantKey;
            if (created.add(endpoint)) {
                createRoute(cartridgeId, variantKey);
            }
        }
    }

    private void createRoute(String cartridgeId, String variantKey) {
        String routeId = variantKey != null 
                ? cartridgeId + "-" + variantKey + "-route"
                : cartridgeId + "-route";
        
        String endpoint = variantKey != null 
                ? "direct:" + cartridgeId + "-" + variantKey
                : "direct:" + cartridgeId;

        from(endpoint)
                .routeId(routeId)
                .bean("persistRaw", "process")
                .bean("schemaValidate", "process")
                .bean("routeResolve", "process")
                .bean("validate", "process")
                .bean("enrich", "process")
                .bean("dtoTransform", "process")
                .bean("externalApiCall", "process")
                .bean("persistTransformed", "process")
                .setHeader("Content-Type", constant("application/json"));
    }

    private String variantKeyFrom(String[] parts) {
        if (parts.length == 4) {
            return null;
        }
        if (parts.length >= 6 && "templates".equals(parts[3])) {
            return parts[4];
        }
        String[] keyParts = Arrays.copyOfRange(parts, 3, parts.length - 1);
        return String.join("_", keyParts);
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
}
