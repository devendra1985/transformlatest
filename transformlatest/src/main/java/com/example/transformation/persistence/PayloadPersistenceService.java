package com.example.transformation.persistence;

import com.example.transformation.persistence.repo.TransformationPayloadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "app.persistence.enabled", havingValue = "true")
@Service
public class PayloadPersistenceService {
  private final DbClient db;
  private final ObjectMapper objectMapper;
  private final TransformationPayloadRepository repo;

  public PayloadPersistenceService(
      DbClient db,
      ObjectMapper objectMapper,
      @Value("${app.persistence.table:TRANSFORMATION_PAYLOADS}") String table
  ) {
    this.db = db;
    this.objectMapper = objectMapper;
    this.repo = new TransformationPayloadRepository(db, table);
  }

  public void storeRaw(String correlationId, Object payload, String status) {
    String json = writeJson(payload);
    db.inTx(() -> repo.upsertRaw(correlationId, json, status, status));
  }

  public void markStep(String correlationId, String status, String step) {
    db.inTx(() -> repo.updateStep(correlationId, status, step));
  }

  public void storeTransformed(String correlationId, Object payload, String status, String step) {
    String json = writeJson(payload);
    db.inTx(() -> repo.updateTransformed(correlationId, json, status, step));
  }

  public void markFailed(String correlationId, String status, String step, String errorCode, String errorMessage) {
    db.inTx(() -> repo.updateFailed(correlationId, status, step, errorCode, errorMessage));
  }

  private String writeJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize payload for persistence", e);
    }
  }
}
