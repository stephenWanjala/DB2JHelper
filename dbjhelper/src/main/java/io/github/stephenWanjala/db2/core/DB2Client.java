package io.github.stephenWanjala.db2.core;

import io.github.stephenWanjala.db2.core.executor.QueryExecutor;
import io.github.stephenWanjala.db2.exception.DataAccessException;
import io.github.stephenWanjala.db2.support.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Main client interface for DB2 operations with connection pooling,
 * transactions, and fluent query building.
 */
public final class DB2Client implements AutoCloseable {
    private final QueryExecutor executor;
    private final DataSource dataSource;
    private final boolean isPooledDataSource;

    public DB2Client(DataSource dataSource) {
        this.dataSource = dataSource;
        this.executor = new QueryExecutor(dataSource);
        this.isPooledDataSource = dataSource != null;
    }

    // Core Operations
    public List<Map<String, Object>> query(String sql, Object... params) {
        return executor.queryForList(sql, params);
    }

    public int update(String sql, Object... params) {
        return executor.update(sql, params);
    }

    public int[] batchUpdate(String sql, List<Object[]> paramsList) {
        return executor.batchUpdate(sql, paramsList);
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = executor.query(sql, mapper, params);
        return results.isEmpty() ? null : results.get(0);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {
        return executor.query(sql, mapper, params);
    }

    // Transaction Management
    public <T> T transaction(Function<Connection, T> action) {
        return transaction(null, action);
    }

    public <T> T transaction(IsolationLevel isolation, Function<Connection, T> action) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            int originalLevel = conn.getTransactionIsolation();

            if (isolation != null) {
                conn.setTransactionIsolation(isolation.getLevelId());
            }

            try {
                T result = action.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Transaction failed", e);
            } finally {
                conn.setTransactionIsolation(originalLevel);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaction management failed", e);
        }
    }

    // Fluent API
    public QueryBuilder select(String... columns) {
        return new QueryBuilder(this).select(columns);
    }

    public QueryBuilder insertInto(String table) {
        return new QueryBuilder(this).insertInto(table);
    }

    @Override
    public void close() {
        if (!isPooledDataSource && dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception e) {
                throw new DataAccessException("Failed to close data source", e);
            }
        }
    }

   /*
    private DataSource createDataSource(DataSourceConfig config) {
        if (config.getDataSource() != null) {
            return config.getDataSource();
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        return new HikariDataSource(hikariConfig);
    }
    */

    public enum IsolationLevel {
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

        private final int levelId;

        IsolationLevel(int levelId) {
            this.levelId = levelId;
        }

        public int getLevelId() {
            return levelId;
        }
    }

    // Fluent Query Builder implementation
    public static class QueryBuilder {
        private final DB2Client client;
        private final StringBuilder sql = new StringBuilder();
        private final List<Object> params = new ArrayList<>();
        private boolean isSelect;

        private QueryBuilder(DB2Client client) {
            this.client = client;
        }

        public QueryBuilder select(String... columns) {
            sql.append("SELECT ").append(String.join(", ", columns));
            isSelect = true;
            return this;
        }

        public QueryBuilder insertInto(String table) {
            sql.append("INSERT INTO ").append(table);
            return this;
        }

        public QueryBuilder values(Map<String, Object> values) {
            sql.append(" (")
                    .append(String.join(", ", values.keySet()))
                    .append(") VALUES (")
                    .append(String.join(", ", Collections.nCopies(values.size(), "?")))
                    .append(")");
            params.addAll(values.values());
            return this;
        }

        public QueryBuilder from(String table) {
            sql.append(" FROM ").append(table);
            return this;
        }

        public QueryBuilder where(String condition, Object... params) {
            sql.append(" WHERE ").append(condition);
            Collections.addAll(this.params, params);
            return this;
        }

        public QueryBuilder orderBy(String order) {
            sql.append(" ORDER BY ").append(order);
            return this;
        }

        public List<Map<String, Object>> list() {
            if (!isSelect) throw new IllegalStateException("Not a SELECT query");
            return client.query(sql.toString(), params.toArray());
        }

        public int execute() {
            return client.update(sql.toString(), params.toArray());
        }
    }
}