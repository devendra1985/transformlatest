package com.example.transformation.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Persistence settings (only used when app.persistence.enabled=true).
 *
 * Keep credentials out of source control; inject via env/secret.
 */
@ConfigurationProperties(prefix = "app.persistence")
public record PersistenceProperties(
    boolean enabled,
    String table,
    String rawTable,
    String transformedTable,
    String jdbcUrl,
    String username,
    String password,
    Integer maxPoolSize,
    Integer minIdle,
    Long connectionTimeoutMs
) {
  public String resolvedTable() {
    if (table != null && !table.isBlank()) {
      return table;
    }
    // Default new single-table design
    return "TRANSFORMATION_PAYLOADS";
  }

  public int resolvedMaxPoolSize() {
    return (maxPoolSize == null || maxPoolSize < 1) ? 10 : maxPoolSize;
  }

  public int resolvedMinIdle() {
    return (minIdle == null || minIdle < 0) ? Math.min(2, resolvedMaxPoolSize()) : minIdle;
  }

  public long resolvedConnectionTimeoutMs() {
    return (connectionTimeoutMs == null || connectionTimeoutMs < 1) ? 5000L : connectionTimeoutMs;
  }
}

