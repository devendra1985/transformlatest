package com.example.transformation.persistence;

import java.util.List;
import java.util.function.Supplier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Small reusable DB wrapper so processors/services don't depend directly on JdbcTemplate.
 *
 * Keep business SQL in repositories.
 */
public class DbClient {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  public DbClient(JdbcTemplate jdbc, TransactionTemplate tx) {
    this.jdbc = jdbc;
    this.tx = tx;
  }

  public int update(String sql, Object... args) {
    return jdbc.update(sql, args);
  }

  public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) {
    return jdbc.batchUpdate(sql, pss);
  }

  public <T> T inTx(Supplier<T> work) {
    return tx.execute(status -> work.get());
  }

  public void inTx(Runnable work) {
    tx.executeWithoutResult(status -> work.run());
  }

  public JdbcTemplate rawJdbc() {
    return jdbc;
  }
}

