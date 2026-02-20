package com.example.transformation.persistence.repo;

import java.sql.Timestamp;

public record PersistenceRecord(
    String requestId,
    String payloadJson,
    String status,
    Timestamp createdAt
) {}

