package io.github.stephenWanjala.db2.core;


import io.github.stephenWanjala.db2.exception.DataAccessException;
import io.github.stephenWanjala.db2.util.RowMapper;
import io.github.stephenWanjala.db2.util.ModelMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * Main client interface for DB2 operations with connection pooling,
 * transactions, and fluent query building.
 */
public final class DB2Client implements AutoCloseable {
    private final QueryExecutor executor;
    private final DataSource dataSource;

    /**
     * Constructs a DB2Client with the given DataSource.
     *
     * @param dataSource the DataSource to use for obtaining connections
     * @throws NullPointerException if the dataSource is null
     */
    public DB2Client(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource must not be null");
        this.executor = new QueryExecutor(dataSource);
    }



    // Core Operations

    /**
     * Executes a query and returns the result as a list of maps, where each map
     * represents a row.
     *
     * @param sql    the SQL statement to execute
     * @param params the parameters to bind to the SQL statement
     * @return a list of maps representing the result set rows
     */
    public List<Map<String, Object>> query(String sql, Object... params) {
        return executor.queryForList(sql, params);
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE) with the given SQL
     * and parameters.
     *
     * @param sql    the SQL statement to execute
     * @param params the parameters to bind to the SQL statement
     * @return the number of rows affected
     */
    public int update(String sql, Object... params) {
        return executor.update(sql, params);
    }

    /**
     * Executes a batch update statement with the given SQL and parameters list.
     *
     * @param sql        the SQL statement to execute
     * @param paramsList a list of parameter arrays to bind to the SQL statement
     * @return an array of update counts for each statement in the batch
     */
    public int[] batchUpdate(String sql, List<Object[]> paramsList) {
        return executor.batchUpdate(sql, paramsList);
    }

    /**
     * Executes a query and maps the first result to an object using the given
     * RowMapper.
     *
     * @param sql    the SQL statement to execute
     * @param mapper the RowMapper to map the result set to an object
     * @param params the parameters to bind to the SQL statement
     * @param <T>    the type of the object to map to
     * @return the mapped object, or null if the result set is empty
     */
    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = executor.query(sql, mapper, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Executes a query and maps the result set to a list of objects using the
     * given RowMapper.
     *
     * @param sql    the SQL statement to execute
     * @param mapper the RowMapper to map the result set to objects
     * @param params the parameters to bind to the SQL statement
     * @param <T>    the type of objects in the result list
     * @return a list of mapped objects
     */
    public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {
        return executor.query(sql, mapper, params);
    }

    /**
     * Executes a query and maps the first result to an object of the specified
     * class using the ModelMapper.
     *
     * @param sql    the SQL statement to execute
     * @param clazz  the class to map the result to
     * @param params the parameters to bind to the SQL statement
     * @param <T>    the type of the object to map to
     * @return the mapped object, or null if the result set is empty
     * @throws DataAccessException if a SQLException or ReflectiveOperationException occurs
     */
    public <T> T queryForObject(String sql, Class<T> clazz, Object... params) {
        try {
            List<T> results = queryForList(sql, clazz, params);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Failed to query for object: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a query and maps the result set to a list of objects of the
     * specified class using the ModelMapper.
     *
     * @param sql    the SQL statement to execute
     * @param clazz  the class to map the results to
     * @param params the parameters to bind to the SQL statement
     * @param <T>    the type of the objects in the result list
     * @return a list of mapped objects
     * @throws DataAccessException if a SQLException or ReflectiveOperationException occurs
     */
    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... params) {
        try {
            return executor.query(
                    sql,
                    rs -> {
                        try {
                            return ModelMapper.populateModel(rs, clazz);
                        } catch (ReflectiveOperationException | SQLException e) {
                            e.printStackTrace();
                            throw new DataAccessException("Failed to map row to object: " + e.getMessage(), e);
                        }
                    },
                    params);
        } catch (DataAccessException e) {
            throw e; // Re-throw DataAccessException
        } catch (Exception e) {
            throw new DataAccessException("Failed to query for list: " + e.getMessage(), e);
        }
    }

    // Transaction Management

    /**
     * Executes a transaction with the given action.
     *
     * @param action the action to execute within the transaction
     * @param <T>    the return type of the action
     * @return the result of the action
     * @throws DataAccessException if the transaction fails
     */
    public <T> T transaction(Function<Connection, T> action) {
        return transaction(null, action);
    }

    /**
     * Executes a transaction with the given isolation level and action.
     *
     * @param isolation the isolation level for the transaction
     * @param action    the action to execute within the transaction
     * @param <T>       the return type of the action
     * @return the result of the action
     * @throws DataAccessException if the transaction fails
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

    // Fluent API

    /**
     * Starts a SELECT query using the fluent API.
     *
     * @param columns the columns to select
     * @return a QueryBuilder instance
     */
    public QueryBuilder select(String... columns) {
        return new QueryBuilder(this).select(columns);
    }

    /**
     * Starts an INSERT query using the fluent API.
     *
     * @param table the table to insert into
     * @return a QueryBuilder instance
     */
    public QueryBuilder insertInto(String table) {
        return new QueryBuilder(this).insertInto(table);
    }

    /**
     * Closes the data source if it's an AutoCloseable and not a pooled data
     * source.
     *
     * @throws DataAccessException if closing the data source fails
     */
    @Override
    public void close() {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception e) {
                throw new DataAccessException("Failed to close data source", e);
            }
        }
    }

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

        /**
         * Appends a SELECT clause to the query.
         *
         * @param columns the columns to select
         * @return this QueryBuilder instance
         */
        public QueryBuilder select(String... columns) {
            sql.append("SELECT ").append(String.join(", ", columns));
            isSelect = true;
            return this;
        }

        /**
         * Appends an INSERT INTO clause to the query.
         *
         * @param table the table to insert into
         * @return this QueryBuilder instance
         */
        public QueryBuilder insertInto(String table) {
            sql.append("INSERT INTO ").append(table);
            return this;
        }

        /**
         * Appends a VALUES clause to the query.
         *
         * @param values a map of column names to values
         * @return this QueryBuilder instance
         */
        public QueryBuilder values(Map<String, Object> values) {
            sql.append(" (")
                    .append(String.join(", ", values.keySet()))
                    .append(") VALUES (")
                    .append(String.join(", ", Collections.nCopies(values.size(), "?")))
                    .append(")");
            params.addAll(values.values());
            return this;
        }

        /**
         * Appends a FROM clause to the query.
         *
         * @param table the table to select from
         * @return this QueryBuilder instance
         */
        public QueryBuilder from(String table) {
            sql.append(" FROM ").append(table);
            return this;
        }

        /**
         * Appends a WHERE clause to the query.
         *
         * @param condition the WHERE condition
         * @param params    the parameters for the WHERE condition
         * @return this QueryBuilder instance
         */
        public QueryBuilder where(String condition, Object... params) {
            sql.append(" WHERE ").append(condition);
            Collections.addAll(this.params, params);
            return this;
        }

        /**
         * Appends an ORDER BY clause to the query.
         *
         * @param order the ORDER BY clause
         * @return this QueryBuilder instance
         */
        public QueryBuilder orderBy(String order) {
            sql.append(" ORDER BY ").append(order);
            return this;
        }

        /**
         * Executes the SELECT query and returns the result as a list of maps.
         *
         * @return a list of maps representing the result set rows
         * @throws IllegalStateException if the query is not a SELECT query
         */
        public List<Map<String, Object>> list() {
            if (!isSelect) {
                throw new IllegalStateException("Not a SELECT query");
            }
            return client.query(sql.toString(), params.toArray());
        }

        /**
         * Executes the query and returns the number of rows affected.
         *
         * @return the number of rows affected
         */
        public int execute() {
            return client.update(sql.toString(), params.toArray());
        }
    }
}
