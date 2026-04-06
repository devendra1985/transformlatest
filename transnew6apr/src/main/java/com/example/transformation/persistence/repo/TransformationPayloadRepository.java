package com.example.transformation.persistence.repo;

import com.example.transformation.persistence.DbClient;
import java.sql.Timestamp;

/**
 * Oracle repository for TRANSFORMATION_PAYLOADS keyed by CORRELATION_ID.
 *
 * Uses MERGE for UPSERT semantics.
 */
public class TransformationPayloadRepository {
  private final DbClient db;
  private final String table;

  public TransformationPayloadRepository(DbClient db, String table) {
    this.db = db;
    this.table = table;
  }

  public void upsertRaw(String correlationId, String rawJson, String status, String step) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(mergeSql("RAW_PAYLOAD"), correlationId, status, step, rawJson, null, null, null, null, now, now);
  }

  public void updateStep(String correlationId, String status, String step) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(
        "update " + table + " set STATUS=?, STEP=?, UPDATED_AT=? where CORRELATION_ID=?",
        status, step, now, correlationId
    );
  }

  public void updateTransformed(String correlationId, String transformedJson, String status, String step) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(
        "update " + table + " set STATUS=?, STEP=?, TRANSFORMED_PAYLOAD=?, UPDATED_AT=? where CORRELATION_ID=?",
        status, step, transformedJson, now, correlationId
    );
  }

  public void updateResponse(String correlationId, String responseJson, String status, String step) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(
        "update " + table + " set STATUS=?, STEP=?, RESPONSE_PAYLOAD=?, UPDATED_AT=? where CORRELATION_ID=?",
        status, step, responseJson, now, correlationId
    );
  }

  public void updateFailed(String correlationId, String status, String step, String errorCode, String errorMessage) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(
        "update " + table + " set STATUS=?, STEP=?, ERROR_CODE=?, ERROR_MESSAGE=?, UPDATED_AT=? where CORRELATION_ID=?",
        status, step, errorCode, truncate(errorMessage, 4000), now, correlationId
    );
  }

  public void updateCounts(String correlationId, int total, int success, int failed) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.update(
        "update " + table + " set TOTAL_COUNT=?, SUCCESS_COUNT=?, FAILED_COUNT=?, UPDATED_AT=? where CORRELATION_ID=?",
        total, success, failed, now, correlationId
    );
  }

  private String mergeSql(String clobColumn) {
    // Parameters:
    // 1  correlationId
    // 2  status
    // 3  step
    // 4  rawPayload
    // 5  transformedPayload
    // 6  errorCode
    // 7  errorMessage
    // 8  createdAt
    // 9  updatedAt
    return ""
        + "merge into " + table + " t "
        + "using (select ? as CORRELATION_ID from dual) s "
        + "on (t.CORRELATION_ID = s.CORRELATION_ID) "
        + "when matched then update set "
        + "  t.STATUS = ?, "
        + "  t.STEP = ?, "
        + "  t." + clobColumn + " = coalesce(?, t." + clobColumn + "), "
        + "  t.TRANSFORMED_PAYLOAD = coalesce(?, t.TRANSFORMED_PAYLOAD), "
        + "  t.RESPONSE_PAYLOAD = coalesce(?, t.RESPONSE_PAYLOAD), "
        + "  t.ERROR_CODE = ?, "
        + "  t.ERROR_MESSAGE = ?, "
        + "  t.UPDATED_AT = ? "
        + "when not matched then insert "
        + "  (CORRELATION_ID, STATUS, STEP, RAW_PAYLOAD, TRANSFORMED_PAYLOAD, RESPONSE_PAYLOAD, ERROR_CODE, ERROR_MESSAGE, CREATED_AT, UPDATED_AT) "
        + "values "
        + "  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  }

  private static String truncate(String s, int max) {
    if (s == null) return null;
    if (s.length() <= max) return s;
    return s.substring(0, max);
  }
}

