package io.github.stephenWanjala.db2.core;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.IsolationLevel;
import io.github.stephenWanjala.db2.config.DataSourceConfig;
import io.github.stephenWanjala.db2.exception.DataAccessException;
import io.github.stephenWanjala.db2.support.RowMapper;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Main client class for DB2 database operations.
 * <p>
 * Handles connection management, query execution, and transactions.
 * Example usage:
 * <pre>{@code
 * try (DB2Client client = new DB2Client(config)) {
 *     List<Map<String, Object>> results =
 *         client.query("SELECT * FROM users WHERE age > ?", 25);
 * }
 * }</pre>
 */
public class DB2Client implements AutoCloseable {
    private final DataSource dataSource;
    private final boolean isPooledDataSource;

    /**
     * Create a DB2Client with the given configuration
     * @param config DataSource configuration
     */
    public DB2Client(DataSourceConfig config) {
        if (config.getDataSource() != null) {
            this.dataSource = config.getDataSource();
            this.isPooledDataSource = true;
        } else {
            this.dataSource = createBasicDataSource(config);
            this.isPooledDataSource = false;
        }
    }

    /**
     * Execute a SQL query and return results as list of maps
     * @param sql SQL query with optional parameters
     * @param params Query parameters
     * @return List of maps representing result rows
     * @throws DataAccessException if any database error occurs
     */
    public List<Map<String, Object>> query(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                return convertResultSetToList(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    /**
     * Execute an update (INSERT/UPDATE/DELETE) operation
     * @param sql SQL statement
     * @param params Statement parameters
     * @return Number of affected rows
     * @throws DataAccessException if any database error occurs
     */
    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        }
    }

    /**
     * Execute operations within a transaction
     * @param transactionLogic Consumer containing transaction operations
     * @throws DataAccessException if any database error occurs
     */
    public void inTransaction(Consumer<Connection> transactionLogic) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionLogic.accept(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Transaction failed", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaction management failed", e);
        }
    }

    /**
     * Close the client and release resources
     */
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


    private DataSource createBasicDataSource(DataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        return new HikariDataSource(hikariConfig);
    }

    private List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                row.put(columnName, rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }


    ///////////////////////
    // Enhanced CRUD Operations
    ///////////////////////

    /**
     * Batch update with optimized processing
     * @param sql SQL statement to execute
     * @param paramsList List of parameter arrays
     * @return Array of update counts
     */
    public int[] batchUpdate(String sql, List<Object[]> paramsList) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Object[] params : paramsList) {
                setParameters(pstmt, params);
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Batch update failed: " + sql, e);
        }
    }

    /**
     * Query for a single object mapped from result set
     * @param sql SQL query
     * @param mapper Row mapper implementation
     * @param params Query parameters
     * @return Mapped object or null
     */
    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = queryForList(sql, mapper, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Query for list of mapped objects
     * @param sql SQL query
     * @param mapper Row mapper implementation
     * @param params Query parameters
     * @return List of mapped objects
     */
    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    ///////////////////////
    // Transaction Management
    ///////////////////////

    /**
     * Execute transaction with configurable isolation level
     * @param isolation Transaction isolation level
     * @param action Transaction logic returning a result
     * @return Result of transaction logic
     */
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

    /**
     * Execute transaction with default isolation level
     * @param action Transaction logic returning a result
     * @return Result of transaction logic
     */
    public <T> T transaction(Function<Connection, T> action) {
        return transaction(null, action);
    }


    /**
     * Simple DataSource implementation for demonstration purposes.
     * In production, replace with a proper connection pool.
     */
    private static class SimpleDataSource implements DataSource {
        private final DataSourceConfig config;

        SimpleDataSource(DataSourceConfig config) {
            this.config = Objects.requireNonNull(config);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(
                    config.getUrl(),
                    config.getUser(),
                    config.getPassword()
            );
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            // For demonstration - in real pool you might want different behavior
            return DriverManager.getConnection(config.getUrl(), username, password);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            // Not implemented for simple datasource
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            // No-op for simple implementation
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            // No-op for simple implementation
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            // Use driver default
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("DataSource of type [" + getClass().getName() +
                    "] cannot be unwrapped as [" + iface.getName() + "]");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.isInstance(this);
        }

        // JDK 9+ compatibility - not required but prevents warnings
        @Override
        public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
    }

}

