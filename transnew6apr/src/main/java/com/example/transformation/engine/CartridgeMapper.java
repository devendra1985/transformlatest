package com.example.transformation.engine;

import com.example.transformation.dto.canonical.CanonicalPayment;

/**
 * Contract for all generated Volante-style mapper cartridges.
 *
 * <p>Each JSON mapping template produces a compile-time generated implementation
 * of this interface. The generated code uses direct getter/setter calls with
 * zero reflection at runtime.
 *
 * @param <T> the target API DTO type (e.g. AccountPayoutRequest2)
 */
public interface CartridgeMapper<T> {

    String templateKey();

    T map(CanonicalPayment source);
}
