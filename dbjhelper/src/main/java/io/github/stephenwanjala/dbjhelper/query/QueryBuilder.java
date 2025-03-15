package io.github.stephenwanjala.dbjhelper.query;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A builder class for creating SQL queries safely.
 */
public class QueryBuilder {
    private final StringBuilder query;
    private final List<Object> parameters;
    private boolean whereAdded;
    private boolean selectMode;
    private boolean insertMode;
    private boolean updateMode;

    /**
     * Creates a SELECT query.
     *
     * @param columns The columns to select
     * @return This builder
     */
    public static QueryBuilder select(String... columns) {
        String columnList = columns.length > 0 ? String.join(", ", columns) : "*";
        QueryBuilder builder = new QueryBuilder("SELECT " + columnList);
        builder.selectMode = true;
        return builder;
    }

    /**
     * Creates an INSERT query.
     *
     * @param table The table to insert into
     * @param columns The columns to insert
     * @return This builder
     */
    public static QueryBuilder insert(@Nonnull String table, String... columns) {
        String columnList = String.join(", ", columns);
        String valuePlaceholders = Arrays.stream(columns)
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        QueryBuilder builder = new QueryBuilder(
                "INSERT INTO " + table + " (" + columnList + ") VALUES (" + valuePlaceholders + ")");
        builder.insertMode = true;
        return builder;
    }

    /**
     * Creates an UPDATE query.
     *
     * @param table The table to update
     * @return This builder
     */
    public static QueryBuilder update(@Nonnull String table) {
        QueryBuilder builder = new QueryBuilder("UPDATE " + table);
        builder.updateMode = true;
        return builder;
    }

    /**
     * Creates a DELETE query.
     *
     * @param table The table to delete from
     * @return This builder
     */
    public static QueryBuilder delete(@Nonnull String table) {
        return new QueryBuilder("DELETE FROM " + table);
    }

    public QueryBuilder(@Nonnull String baseQuery) {
        this.query = new StringBuilder(baseQuery);
        this.parameters = new ArrayList<>();
        this.whereAdded = false;
    }

    /**
     * Adds a FROM clause to the query.
     *
     * @param table The table name
     * @return This builder
     */
    public QueryBuilder from(@Nonnull String table) {
        if (selectMode) {
            query.append(" FROM ").append(table);
        }
        return this;
    }

    /**
     * Sets values for UPDATE query.
     *
     * @param assignments The column assignments
     * @param values The values to set
     * @return This builder
     */
    public QueryBuilder set(@Nonnull String assignments, Object... values) {
        if (updateMode) {
            query.append(" SET ").append(assignments);
            if (values != null) {
                Collections.addAll(parameters, values);
            }
        }
        return this;
    }

    /**
     * Adds a WHERE clause to the query.
     *
     * @param condition The condition to add
     * @param params The parameters for the condition
     * @return This builder
     */
    public QueryBuilder where(@Nonnull String condition, Object... params) {
        if (!whereAdded) {
            query.append(" WHERE ");
            whereAdded = true;
        } else {
            query.append(" AND ");
        }
        query.append(condition);
        if (params != null) {
            Collections.addAll(parameters, params);
        }
        return this;
    }

    /**
     * Adds an AND clause to the query.
     *
     * @param condition The condition to add
     * @param params The parameters for the condition
     * @return This builder
     */
    public QueryBuilder and(@Nonnull String condition, Object... params) {
        return where(condition, params);
    }

    /**
     * Adds an OR clause to the query.
     *
     * @param condition The condition to add
     * @param params The parameters for the condition
     * @return This builder
     */
    public QueryBuilder or(@Nonnull String condition, Object... params) {
        if (whereAdded) {
            query.append(" OR ");
        } else {
            query.append(" WHERE ");
            whereAdded = true;
        }
        query.append(condition);
        if (params != null) {
            Collections.addAll(parameters, params);
        }
        return this;
    }

    /**
     * Adds a GROUP BY clause to the query.
     *
     * @param columns The columns to group by
     * @return This builder
     */
    public QueryBuilder groupBy(@Nonnull String... columns) {
        query.append(" GROUP BY ").append(String.join(", ", columns));
        return this;
    }

    /**
     * Adds a HAVING clause to the query.
     *
     * @param condition The condition to add
     * @param params The parameters for the condition
     * @return This builder
     */
    public QueryBuilder having(@Nonnull String condition, Object... params) {
        query.append(" HAVING ").append(condition);
        if (params != null) {
            Collections.addAll(parameters, params);
        }
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query.
     *
     * @param columns The columns to order by
     * @return This builder
     */
    public QueryBuilder orderBy(@Nonnull String... columns) {
        query.append(" ORDER BY ").append(String.join(", ", columns));
        return this;
    }

    /**
     * Adds a LIMIT clause to the query.
     *
     * @param limit The maximum number of rows to return
     * @return This builder
     */
    public QueryBuilder limit(int limit) {
        query.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
        return this;
    }

    /**
     * Gets the built query string.
     *
     * @return The query string
     */
    public String getQuery() {
        return query.toString();
    }

    /**
     * Gets the parameters for the query.
     *
     * @return The list of parameters
     */
    public List<Object> getParameters() {
        return new ArrayList<>(parameters);
    }
} 