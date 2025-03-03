package io.github.stephenWanjala.db2.support;

import io.github.stephenWanjala.db2.core.DB2Client;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional interface for mapping rows of a ResultSet to objects.
 * <p>
 * Implementations of this interface perform the actual work of mapping
 * each row of data in a ResultSet to a result object. Typically used
 * in query methods of {@link DB2Client}.
 *
 * @param <T> the type of object to map to
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * RowMapper<User> userMapper = rs -> {
 *     User user = new User();
 *     user.setId(rs.getInt("id"));
 *     user.setName(rs.getString("name"));
 *     return user;
 * };
 * }</pre>
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Map a single row of the ResultSet to an object.
     *
     * @param rs the ResultSet to map (positioned at the current row)
     * @return the mapped object
     * @throws SQLException if a SQLException is encountered while processing
     *                      the ResultSet (i.e., no need to catch it)
     */
    T mapRow(ResultSet rs) throws SQLException;
}