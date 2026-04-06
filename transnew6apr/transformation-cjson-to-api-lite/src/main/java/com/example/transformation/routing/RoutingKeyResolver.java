package com.example.transformation.routing;

import com.example.transformation.config.ConfigLoader;
import com.example.transformation.config.TemplateSelectionProperties;
import com.example.transformation.dto.canonical.CanonicalPayment;
import com.example.transformation.dto.canonical.CanonicalPayment.CanonicalTransaction;

public class RoutingKeyResolver {
  private final TemplateSelectionProperties templateSelectionProperties;
  private final ConfigLoader configLoader;

  public RoutingKeyResolver(
      TemplateSelectionProperties templateSelectionProperties,
      ConfigLoader configLoader) {
    this.templateSelectionProperties = templateSelectionProperties;
    this.configLoader = configLoader;
  }

  public RoutingKey resolve(CanonicalPayment payment) {
    RoutingKey key = new RoutingKey();
    CanonicalTransaction tx = payment != null ? payment.getTransaction() : null;
    if (tx != null) {
      String schemaType = firstNonBlank(tx.getSchemaType(), tx.getPaymentSchema());
      String cartridgeCode = firstNonBlank(tx.getCartridgeCode(), resolveCartridgeCode(schemaType));
      key.setCartridgeCode(cartridgeCode);
      key.setPaymentSchema(schemaType);
      key.setCountry(tx.getCountry());
      key.setCurrency(tx.getCurrency());
      key.setSegment(mapSegment(tx.getRecipientType()));
    }
    return key;
  }

  private String mapSegment(String recipientType) {
    if (recipientType == null || recipientType.isBlank()) {
      return null;
    }
    if (templateSelectionProperties.recipientTypeIndividual().equalsIgnoreCase(recipientType)) {
      return templateSelectionProperties.segmentIndividual();
    }
    if (templateSelectionProperties.recipientTypeBusiness().equalsIgnoreCase(recipientType)) {
      return templateSelectionProperties.segmentBusiness();
    }
    return null;
  }

  private String resolveCartridgeCode(String cartridgeId) {
    String cartridgeCode = configLoader.getCartridgeMasterConfig().findCartridgeCode(cartridgeId);
    if (cartridgeCode == null || cartridgeCode.isBlank()) {
      throw new IllegalArgumentException("No cartridgeCode configured for cartridgeId: " + cartridgeId);
    }
    return cartridgeCode;
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
