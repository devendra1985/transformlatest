package com.example.transformation.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(name = "app.persistence.enabled", havingValue = "true")
@EnableConfigurationProperties(PersistenceProperties.class)
public class PersistenceConfig {

  @Bean(destroyMethod = "close")
  public DataSource dataSource(PersistenceProperties props) {
    HikariConfig cfg = new HikariConfig();
    cfg.setJdbcUrl(props.jdbcUrl());
    cfg.setUsername(props.username());
    cfg.setPassword(props.password());

    cfg.setMaximumPoolSize(props.resolvedMaxPoolSize());
    cfg.setMinimumIdle(props.resolvedMinIdle());
    cfg.setConnectionTimeout(props.resolvedConnectionTimeoutMs());

    // Helpful defaults for JDBC workloads
    cfg.setAutoCommit(true);
    cfg.setPoolName("transform-hikari");

    return new HikariDataSource(cfg);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public TransactionTemplate transactionTemplate(PlatformTransactionManager txManager) {
    return new TransactionTemplate(txManager);
  }

  @Bean
  public DbClient dbClient(JdbcTemplate jdbcTemplate, TransactionTemplate txTemplate) {
    return new DbClient(jdbcTemplate, txTemplate);
  }
}

