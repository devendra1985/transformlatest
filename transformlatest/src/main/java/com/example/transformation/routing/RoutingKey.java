package com.example.transformation.routing;

import lombok.Data;

@Data
public class RoutingKey {
  private String cartridgeCode;
  private String paymentSchema;
  private String country;
  private String currency;
  private String segment;

  public String asKey(String format) {
    return format
        .replace("{cartridgeCode}", nullToEmpty(cartridgeCode))
        .replace("{paymentSchema}", nullToEmpty(paymentSchema))
        .replace("{country}", nullToEmpty(country))
        .replace("{currency}", nullToEmpty(currency))
        .replace("{segment}", nullToEmpty(segment));
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
