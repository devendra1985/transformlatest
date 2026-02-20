package com.example.transformation.config.model;

import java.util.List;
import java.util.Map;

/**
 * Model for cartridge-master.yaml
 * Loaded via Jackson YAML ObjectMapper.
 */
public record CartridgeMasterConfig(
        Map<String, ProviderConfig> providers,
        Map<String, String> cartridgeCodeFlows,
        Map<String, CartridgeSelection> cartridgeSelections) {

    public CartridgeMasterConfig {
        providers = providers != null ? Map.copyOf(providers) : Map.of();
        cartridgeCodeFlows = cartridgeCodeFlows != null
                ? Map.copyOf(cartridgeCodeFlows)
                : Map.of();
        cartridgeSelections = cartridgeSelections != null
                ? Map.copyOf(cartridgeSelections)
                : Map.of();
    }

    public record ProviderConfig(String name, List<CartridgeConfig> cartridges) {
        public ProviderConfig {
            cartridges = cartridges != null ? List.copyOf(cartridges) : List.of();
        }
    }

    public record CartridgeConfig(String id, String cartridgeCode) {}

    public record CartridgeSelection(String cartridgeId, String flowId) {}

    public String findCartridgeCode(String cartridgeId) {
        if (cartridgeId == null || cartridgeId.isBlank()) {
            return null;
        }
        for (ProviderConfig provider : providers.values()) {
            for (CartridgeConfig cartridge : provider.cartridges()) {
                if (cartridgeId.equalsIgnoreCase(cartridge.id())) {
                    return cartridge.cartridgeCode();
                }
            }
        }
        return null;
    }

    public String resolveFlowIdForCartridge(String cartridgeId) {
        String cartridgeCode = findCartridgeCode(cartridgeId);
        return resolveFlowIdForCode(cartridgeCode);
    }

    public String resolveFlowIdForCode(String cartridgeCode) {
        if (cartridgeCode == null || cartridgeCode.isBlank()) {
            return null;
        }
        return cartridgeCodeFlows.get(cartridgeCode);
    }

    public CartridgeSelection resolveSelection(String cartridgeCode, String apiType, String schemaType) {
        String key = selectionKey(cartridgeCode, apiType, schemaType);
        return key == null ? null : cartridgeSelections.get(key);
    }

    private String selectionKey(String cartridgeCode, String apiType, String schemaType) {
        if (cartridgeCode == null || cartridgeCode.isBlank()) {
            return null;
        }
        if (apiType == null || apiType.isBlank()) {
            return null;
        }
        if (schemaType == null || schemaType.isBlank()) {
            return null;
        }
        return cartridgeCode + "|" + apiType + "|" + schemaType;
    }
}
