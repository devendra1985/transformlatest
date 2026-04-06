package com.example.transformation.fieldset;

import com.example.transformation.routing.RoutingKey;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Loads field-set configuration from FIELD_SET_CONFIG scoped by
 * CARTRIDGE_CODE + PAYMENT_SCHEMA, then resolves the required field set
 * for any country/currency/segment combination.
 *
 * <p>Resolution: COMMON ∪ CORRIDOR(country|currency) ∪ SEGMENT ∪ COMBO(country|currency|segment)
 *
 * <p>CARTRIDGE_CODE and PAYMENT_SCHEMA are row identifiers (not resolution dimensions) —
 * they scope which set of rows to load so VISA and DANDALION each have their own config.
 */
@Component
public class FieldSetResolver {

    private static final Logger log = LoggerFactory.getLogger(FieldSetResolver.class);

    private static final String LOAD_BY_CARTRIDGE =
        "SELECT DIMENSION, DIMENSION_KEY, FIELD_NAME "
            + "FROM FIELD_SET_CONFIG "
            + "WHERE CARTRIDGE_CODE = ? AND PAYMENT_SCHEMA = ? AND ACTIVE = 'Y' "
            + "ORDER BY DIMENSION, DIMENSION_KEY, FIELD_NAME";

    private final JdbcTemplate jdbc;
    private final Map<String, AtomicReference<Snapshot>> snapshots = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> cache = new ConcurrentHashMap<>();

    public FieldSetResolver(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── public API ──────────────────────────────────────────────────────────

    public Set<String> resolve(RoutingKey routingKey) {
        return resolve(
            routingKey.getCartridgeCode(),
            routingKey.getPaymentSchema(),
            routingKey.getCountry(),
            routingKey.getCurrency(),
            routingKey.getSegment());
    }

    public Set<String> resolve(String cartridgeCode, String paymentSchema,
                               String country, String currency, String segment) {
        String cacheKey = n(cartridgeCode) + "|" + n(paymentSchema) + "|"
            + n(country) + "|" + n(currency) + "|" + n(segment);
        return cache.computeIfAbsent(cacheKey, k ->
            compose(cartridgeCode, paymentSchema, country, currency, segment));
    }

    /**
     * Hot-reload for a specific cartridge+schema. Clears the cache so next
     * resolve() re-computes from fresh DB data.
     */
    public void refresh(String cartridgeCode, String paymentSchema) {
        String scopeKey = n(cartridgeCode) + "|" + n(paymentSchema);
        snapshots.put(scopeKey, new AtomicReference<>(loadFromDb(cartridgeCode, paymentSchema)));
        cache.clear();
        log.info("FieldSetResolver refreshed for {}", scopeKey);
    }

    // ── internals ───────────────────────────────────────────────────────────

    private Snapshot getSnapshot(String cartridgeCode, String paymentSchema) {
        String scopeKey = n(cartridgeCode) + "|" + n(paymentSchema);
        return snapshots
            .computeIfAbsent(scopeKey, k ->
                new AtomicReference<>(loadFromDb(cartridgeCode, paymentSchema)))
            .get();
    }

    private Set<String> compose(String cartridgeCode, String paymentSchema,
                                String country, String currency, String segment) {
        Snapshot snap = getSnapshot(cartridgeCode, paymentSchema);
        Set<String> result = new LinkedHashSet<>(snap.common);
        String corridorKey = n(country) + "|" + n(currency);
        result.addAll(snap.corridor.getOrDefault(corridorKey, Collections.emptySet()));
        result.addAll(snap.segment.getOrDefault(n(segment), Collections.emptySet()));
        String comboKey = n(country) + "|" + n(currency) + "|" + n(segment);
        result.addAll(snap.combo.getOrDefault(comboKey, Collections.emptySet()));
        return Collections.unmodifiableSet(result);
    }

    private Snapshot loadFromDb(String cartridgeCode, String paymentSchema) {
        Map<String, Map<String, Set<String>>> grouped = new LinkedHashMap<>();

        List<Object[]> rows = jdbc.query(LOAD_BY_CARTRIDGE,
            (rs, i) -> new Object[]{
                rs.getString("DIMENSION"),
                rs.getString("DIMENSION_KEY"),
                rs.getString("FIELD_NAME")
            },
            n(cartridgeCode), n(paymentSchema));

        for (Object[] row : rows) {
            grouped
                .computeIfAbsent((String) row[0], k -> new LinkedHashMap<>())
                .computeIfAbsent((String) row[1], k -> new LinkedHashSet<>())
                .add((String) row[2]);
        }

        Snapshot snap = new Snapshot(
            unmod(grouped.getOrDefault("COMMON", Map.of()).getOrDefault("COMMON", Set.of())),
            unmodMap(grouped.getOrDefault("CORRIDOR", Map.of())),
            unmodMap(grouped.getOrDefault("SEGMENT", Map.of())),
            unmodMap(grouped.getOrDefault("COMBO", Map.of()))
        );

        log.info("FieldSetResolver loaded [{}|{}]: common={}, corridors={}, segments={}, combos={}",
            cartridgeCode, paymentSchema,
            snap.common.size(), snap.corridor.size(), snap.segment.size(), snap.combo.size());
        return snap;
    }

    private static String n(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static Set<String> unmod(Set<String> set) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(set));
    }

    private static Map<String, Set<String>> unmodMap(Map<String, Set<String>> map) {
        return Collections.unmodifiableMap(map);
    }

    private record Snapshot(
        Set<String> common,
        Map<String, Set<String>> corridor,
        Map<String, Set<String>> segment,
        Map<String, Set<String>> combo
    ) {}
}
