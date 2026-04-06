package com.example.transformation.processor;

public final class ExchangeKeys {
  private ExchangeKeys() {}

  // ── Outbound / shared ────────────────────────────────────────────────────
  public static final String CARTRIDGE_ID_HEADER = "X-Cartridge-Id";
  public static final String CURRENCY_HEADER = "X-Currency";
  public static final String TEMPLATE_HEADER = "X-Template";
  public static final String DIRECTION_HEADER = "X-Direction";
  public static final String FLOW_ID_HEADER = "X-Flow-Id";
  public static final String RESOLVED_CONTEXT_PROP = "resolvedCartridgeContext";
  public static final String CARTRIDGE_BUNDLE_PROP = "cartridgeBundle";
  public static final String REQUIRED_FIELD_SET_PROP = "requiredFieldSet";
  public static final String ROUTING_KEY_PROP = "routingKey";
  public static final String CANONICAL_PAYMENT_PROP = "canonicalPayment";

  // ── Inbound webhook ──────────────────────────────────────────────────────
  /** Vendor identifier forwarded from the gateway (e.g. {@code VISA}). */
  public static final String VENDOR_ID_HEADER = "X-Vendor-Id";
  /** Vendor-defined event type (e.g. {@code PAYMENT_INBOUND}). */
  public static final String WEBHOOK_EVENT_HEADER = "X-Webhook-Event";
  /** HMAC-SHA256 signature from the vendor for payload integrity (e.g. {@code sha256=...}). */
  public static final String VENDOR_SIGNATURE_HEADER = "X-Vendor-Signature";
  /** Direction value for inbound flows. */
  public static final String DIRECTION_INBOUND = "inbound";
}

