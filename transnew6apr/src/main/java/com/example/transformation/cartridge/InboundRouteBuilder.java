package com.example.transformation.cartridge;

import com.example.transformation.config.ConfigLoader;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registers one Camel route per cartridge for <strong>inbound</strong> webhook processing.
 *
 * <p>Route naming convention: {@code direct:inbound-{cartridgeId}}
 * <br>Route ID convention: {@code inbound-{cartridgeId}-route}
 *
 * <p>Inbound pipeline (per route):
 * <pre>
 *   direct:inbound-{cartridgeId}
 *       └─ inboundPersist   ← persist raw payload + build WebhookAckResponse
 *       └─ Content-Type: application/json
 * </pre>
 *
 * <p>New cartridges are picked up automatically from {@code schema-master.yaml} at startup –
 * no code change is required when a new cartridgeId is onboarded.
 */
@Component
public class InboundRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(InboundRouteBuilder.class);

    private final ConfigLoader configLoader;

    public InboundRouteBuilder(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public void configure() {
        var cartridges = configLoader.getSchemaMasterConfig().cartridges();
        if (cartridges == null || cartridges.isEmpty()) {
            log.warn("No cartridges found in schema master config – no inbound routes will be registered");
            return;
        }

        for (String cartridgeId : cartridges.keySet()) {
            String endpoint = "direct:inbound-" + cartridgeId;
            String routeId = "inbound-" + cartridgeId + "-route";

            from(endpoint)
                    .routeId(routeId)
                    .bean("inboundPersist", "process")
                    .setHeader("Content-Type", constant("application/json"));

            log.info("Registered inbound route: {} ({})", routeId, endpoint);
        }
    }
}
