package io.github.stephenWanjala.db2.core.executor;


import io.github.stephenWanjala.db2.exception.DataAccessException;
import io.github.stephenWanjala.db2.support.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes SQL queries and updates, handling JDBC resource management and exception translation.
 * <p>
 * This class serves as the core component for database operations, providing methods for:
 * <ul>
 *   <li>Executing UPDATE/INSERT/DELETE statements</li>
 *   <li>Processing SELECT queries with result set mapping</li>
 *   <li>Managing batch operations</li>
 *   <li>Handling database connections and transactions</li>
 * </ul>
 */
public class QueryExecutor {
    private final DataSource dataSource;

    /**
     * Constructs a new QueryExecutor with the specified DataSource
     *
     * @param dataSource The DataSource for database connections
     */
    public QueryExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes an update statement (INSERT/UPDATE/DELETE)
     *
     * @param sql    The SQL statement
     * @param params Statement parameters
     * @return Number of affected rows
     * @throws DataAccessException if a database error occurs
     */
    public int update(String sql, Object... params) {
        return executeUpdate(sql, null, params);
    }

    /**
     * Executes an update statement within an existing transaction
     *
     * @param sql    The SQL statement
     * @param conn   Existing database connection
     * @param params Statement parameters
     * @return Number of affected rows
     * @throws DataAccessException if a database error occurs
     */
    public int update(String sql, Connection conn, Object... params) {
        return executeUpdate(sql, conn, params);
    }

    /**
     * Executes a batch update operation
     *
     * @param sql        The SQL statement
     * @param paramsList List of parameter arrays
     * @return Array of update counts
     * @throws DataAccessException if a database error occurs
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
     * Executes a query and returns mapped results
     *
     * @param sql    The SQL query
     * @param mapper RowMapper implementation
     * @param params Query parameters
     * @return List of mapped objects
     * @throws DataAccessException if a database error occurs
     */
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
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

    /**
     * Executes a query and returns results as maps
     *
     * @param sql    The SQL query
     * @param params Query parameters
     * @return List of column maps
     * @throws DataAccessException if a database error occurs
     */
    public List<Map<String, Object>> queryForList(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                return convertResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    private int executeUpdate(String sql, Connection existingConn, Object... params) {
        Connection conn = null;
        boolean closeConnection = existingConn == null;

        try {
            conn = existingConn != null ? existingConn : dataSource.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        } finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Log warning but don't propagate
                }
            }
        }
    }

    private List<Map<String, Object>> convertResultSet(ResultSet rs) throws SQLException {
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
}