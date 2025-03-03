package io.github.stephenWanjala.db2.core.executor;

import io.github.stephenWanjala.db2.exception.DataAccessException;
import io.github.stephenWanjala.db2.support.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Core executor handling SQL operations with JDBC. Manages connections,
 * statement preparation, and result set processing.
 */
 public final class QueryExecutor {
    private final DataSource dataSource;

    /**
     * Constructs a QueryExecutor with the given DataSource.
     *
     * @param dataSource the DataSource to use for obtaining connections
     * @throws NullPointerException if the dataSource is null
     */
    public QueryExecutor(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource must not be null");
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE) with the given SQL and parameters.
     *
     * @param sql    the SQL statement to execute
     * @param params the parameters to bind to the SQL statement
     * @return the number of rows affected
     */
    public int update(String sql, Object... params) {
        return executeUpdate(sql, null, params);
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE) with the given SQL, connection, and parameters.
     *
     * @param sql    the SQL statement to execute
     * @param conn   the Connection to use for the update
     * @param params the parameters to bind to the SQL statement
     * @return the number of rows affected
     */
    public int update(String sql, Connection conn, Object... params) {
        return executeUpdate(sql, conn, params);
    }

    /**
     * Executes a batch update statement with the given SQL and parameters list.
     *
     * @param sql        the SQL statement to execute
     * @param paramsList a list of parameter arrays to bind to the SQL statement
     * @return an array of update counts for each statement in the batch
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
            throw new DataAccessException("Batch update failed: " + sanitizeSql(sql), e);
        }
    }

    /**
     * Executes a query and maps the result set to a list of objects using the given RowMapper.
     *
     * @param sql    the SQL statement to execute
     * @param mapper the RowMapper to map the result set to objects
     * @param params the parameters to bind to the SQL statement
     * @param <T>    the type of objects in the result list
     * @return a list of mapped objects
     */
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {

            return executeQuery(pstmt, mapper);
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sanitizeSql(sql), e);
        }
    }

    /**
     * Executes a query and converts the result set to a list of maps, where each map represents a row.
     *
     * @param sql    the SQL statement to execute
     * @param params the parameters to bind to the SQL statement
     * @return a list of maps representing the result set rows
     */
    public List<Map<String, Object>> queryForList(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = prepareStatement(conn, sql, params)) {

            return convertResultSet(pstmt.executeQuery());
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sanitizeSql(sql), e);
        }
    }

    private int executeUpdate(String sql, Connection existingConn, Object... params) {
        boolean closeConnection = existingConn == null;
        Connection conn = null;

        try {
            conn = existingConn != null ? existingConn : dataSource.getConnection();
            try (PreparedStatement pstmt = prepareStatement(conn, sql, params)) {
                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sanitizeSql(sql), e);
        } finally {
            closeConnectionSafely(conn, closeConnection);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, RowMapper<T> mapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.mapRow(rs));
            }
            return results;
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setParameters(pstmt, params);
        return pstmt;
    }

    private List<Map<String, Object>> convertResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
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

    private void closeConnectionSafely(Connection conn, boolean shouldClose) {
        if (shouldClose && conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // Log warning
            }
        }
    }

    private String sanitizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
