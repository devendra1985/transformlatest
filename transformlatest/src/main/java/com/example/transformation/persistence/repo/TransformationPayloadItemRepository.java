package com.example.transformation.persistence.repo;

import com.example.transformation.persistence.DbClient;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

/**
 * Oracle repository for TRANSFORMATION_PAYLOAD_ITEMS keyed by (CORRELATION_ID, TX_INDEX).
 */
public class TransformationPayloadItemRepository {
  private final DbClient db;
  private final String table;

  public TransformationPayloadItemRepository(DbClient db, String table) {
    this.db = db;
    this.table = table;
  }

  public void upsertItems(String correlationId, List<ItemRow> rows) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.batchUpdate(mergeSql(), new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
        ItemRow r = rows.get(i);
        // merge match key
        ps.setString(1, correlationId);
        ps.setInt(2, r.txIndex());

        // update set
        ps.setString(3, r.status());
        ps.setString(4, r.step());
        ps.setString(5, r.paymentId());
        ps.setString(6, r.rawTxInfJson());
        ps.setString(7, r.transformedJson());
        ps.setString(8, r.responseJson());
        ps.setString(9, r.errorCode());
        ps.setString(10, truncate(r.errorMessage(), 4000));
        ps.setTimestamp(11, now);

        // insert values
        ps.setString(11, correlationId);
        ps.setInt(12, r.txIndex());
        ps.setString(13, r.paymentId());
        ps.setString(14, r.status());
        ps.setString(15, r.step());
        ps.setString(16, r.rawTxInfJson());
        ps.setString(17, r.transformedJson());
        ps.setString(18, r.responseJson());
        ps.setString(19, r.errorCode());
        ps.setString(20, truncate(r.errorMessage(), 4000));
        ps.setInt(21, r.retryCount());
        ps.setTimestamp(22, now);
        ps.setTimestamp(23, now);
      }

      @Override
      public int getBatchSize() {
        return rows.size();
      }
    });
  }

  public void updateByPaymentId(String correlationId, List<ItemStatusUpdate> updates) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    db.batchUpdate(
        "update " + table + " set STATUS=?, STEP=?, RESPONSE_PAYLOAD=?, ERROR_CODE=?, ERROR_MESSAGE=?, UPDATED_AT=? "
            + "where CORRELATION_ID=? and PAYMENT_ID=?",
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
            ItemStatusUpdate u = updates.get(i);
            ps.setString(1, u.status());
            ps.setString(2, u.step());
            ps.setString(3, u.responseJson());
            ps.setString(4, u.errorCode());
            ps.setString(5, truncate(u.errorMessage(), 4000));
            ps.setTimestamp(6, now);
            ps.setString(7, correlationId);
            ps.setString(8, u.paymentId());
          }

          @Override
          public int getBatchSize() {
            return updates.size();
          }
        }
    );
  }

  public record ItemRow(
      int txIndex,
      String paymentId,
      String status,
      String step,
      String rawTxInfJson,
      String transformedJson,
      String responseJson,
      String errorCode,
      String errorMessage,
      int retryCount
  ) {}

  public record ItemStatusUpdate(
      String paymentId,
      String status,
      String step,
      String responseJson,
      String errorCode,
      String errorMessage
  ) {}

  private String mergeSql() {
    return ""
        + "merge into " + table + " t "
        + "using (select ? as CORRELATION_ID, ? as TX_INDEX from dual) s "
        + "on (t.CORRELATION_ID = s.CORRELATION_ID and t.TX_INDEX = s.TX_INDEX) "
        + "when matched then update set "
        + "  t.STATUS = ?, "
        + "  t.STEP = ?, "
        + "  t.PAYMENT_ID = coalesce(?, t.PAYMENT_ID), "
        + "  t.RAW_TXINF = coalesce(?, t.RAW_TXINF), "
        + "  t.TRANSFORMED_PAYLOAD = ?, "
        + "  t.RESPONSE_PAYLOAD = ?, "
        + "  t.ERROR_CODE = ?, "
        + "  t.ERROR_MESSAGE = ?, "
        + "  t.UPDATED_AT = ? "
        + "when not matched then insert "
        + "  (CORRELATION_ID, TX_INDEX, PAYMENT_ID, STATUS, STEP, RAW_TXINF, TRANSFORMED_PAYLOAD, RESPONSE_PAYLOAD, ERROR_CODE, ERROR_MESSAGE, RETRY_COUNT, CREATED_AT, UPDATED_AT) "
        + "values "
        + "  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  }

  private static String truncate(String s, int max) {
    if (s == null) return null;
    if (s.length() <= max) return s;
    return s.substring(0, max);
  }
}

