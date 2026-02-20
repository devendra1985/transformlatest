package com.example.transformation.persistence.repo;

import com.example.transformation.persistence.DbClient;
import java.sql.PreparedStatement;
import java.util.List;

public class TransformOutRepository {
  private final DbClient db;
  private final String table;

  public TransformOutRepository(DbClient db, String table) {
    this.db = db;
    this.table = table;
  }

  public void insert(PersistenceRecord r) {
    db.update(
        "insert into " + table + " (REQUEST_ID, TRANSFORMED_PAYLOAD, STATUS, CREATED_AT) values (?, ?, ?, ?)",
        r.requestId(),
        r.payloadJson(),
        r.status(),
        r.createdAt()
    );
  }

  public void insertBatch(List<PersistenceRecord> records) {
    db.batchUpdate(
        "insert into " + table + " (REQUEST_ID, TRANSFORMED_PAYLOAD, STATUS, CREATED_AT) values (?, ?, ?, ?)",
        new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
            PersistenceRecord r = records.get(i);
            ps.setString(1, r.requestId());
            ps.setString(2, r.payloadJson());
            ps.setString(3, r.status());
            ps.setTimestamp(4, r.createdAt());
          }

          @Override
          public int getBatchSize() {
            return records.size();
          }
        }
    );
  }
}

