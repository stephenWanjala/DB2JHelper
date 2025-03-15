package io.github.stephenwanjala.dbjhelper.query;

import io.github.stephenwanjala.dbjhelper.exception.DB2HelperException;
import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.function.Function;

/**
 * Executes SQL queries and manages results and transactions.
 */
public class QueryExecutor implements AutoCloseable {
    private final Connection connection;

    public QueryExecutor(@Nonnull Connection connection) {
        this.connection = connection;
    }

    /**
     * Executes a query and maps the results using the provided mapper function.
     *
     * @param query The query to execute
     * @param mapper The function to map ResultSet rows to objects
     * @param params The query parameters
     * @return List of mapped objects
     * @throws DB2HelperException if the query fails
     */
    public <T> List<T> executeQuery(@Nonnull String query, 
                                   @Nonnull Function<ResultSet, T> mapper, 
                                   Object... params) {
        try (PreparedStatement stmt = prepareStatement(query, params);
             ResultSet rs = stmt.executeQuery()) {
            
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.apply(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to execute query: " + query, e);
        }
    }

    /**
     * Executes a query and returns results as a list of maps.
     *
     * @param query The query to execute
     * @param params The query parameters
     * @return List of maps where each map represents a row
     */
    public List<Map<String, Object>> query(@Nonnull String query, Object... params) {
        try (PreparedStatement stmt = prepareStatement(query, params);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to execute query: " + query, e);
        }
    }

    /**
     * Executes a query and maps the first result to the specified type.
     *
     * @param query The query to execute
     * @param type The class to map to
     * @param params The query parameters
     * @return The mapped object or null if no results
     */
    public <T> T queryForObject(@Nonnull String query, @Nonnull Class<T> type, Object... params) {
        List<T> results = queryForList(query, type, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Executes a query and maps all results to the specified type.
     *
     * @param query The query to execute
     * @param type The class to map to
     * @param params The query parameters
     * @return List of mapped objects
     */
    public <T> List<T> queryForList(@Nonnull String query, @Nonnull Class<T> type, Object... params) {
        try (PreparedStatement stmt = prepareStatement(query, params);
             ResultSet rs = stmt.executeQuery()) {

            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(ModelMapper.populateModel(rs, type));
            }
            return results;
        } catch (Exception e) {
            throw new DB2HelperException("Failed to execute query for type " + type.getName(), e);
        }
    }



    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     *
     * @param query The query to execute
     * @param params The query parameters
     * @return Number of affected rows
     */
    public int executeUpdate(@Nonnull String query, Object... params) {
        try (PreparedStatement stmt = prepareStatement(query, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to execute update: " + query, e);
        }
    }

    /**
     * Executes a batch update query.
     *
     * @param query The query to execute
     * @param paramsList List of parameter arrays for batch execution
     * @return Array of update counts
     */
    public int[] executeBatch(@Nonnull String query, @Nonnull List<Object[]> paramsList) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (Object[] params : paramsList) {
                setParameters(stmt, params);
                stmt.addBatch();
            }
            return stmt.executeBatch();
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to execute batch update: " + query, e);
        }
    }

    /**
     * Executes operations within a transaction.
     *
     * @param action The operations to execute
     * @return The result of the transaction
     */
    public <T> T transaction(@Nonnull TransactionCallback<T> action) {
        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to start transaction", e);
        }

        try {
            T result = action.execute(this);
            connection.commit();
            return result;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DB2HelperException("Failed to rollback transaction", ex);
            }
            throw new DB2HelperException("Transaction failed", e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                throw new DB2HelperException("Failed to restore auto-commit setting", e);
            }
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to close connection", e);
        }
    }

    private PreparedStatement prepareStatement(String query, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        setParameters(stmt, params);
        return stmt;
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T mapResultSetToType(ResultSet rs, Class<T> type) throws Exception {
        if (Map.class.isAssignableFrom(type)) {
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), rs.getObject(i));
            }
            return (T) map;
        }
        
        T instance = type.getDeclaredConstructor().newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            try {
                String setterName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                type.getMethod(setterName, rs.getObject(i).getClass()).invoke(instance, rs.getObject(i));
            } catch (NoSuchMethodException ignored) {
                // Skip if no matching setter found
            }
        }
        return instance;
    }
}



