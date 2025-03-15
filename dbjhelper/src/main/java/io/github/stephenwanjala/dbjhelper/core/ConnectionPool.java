package io.github.stephenwanjala.dbjhelper.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.stephenwanjala.dbjhelper.exception.DB2HelperException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages a pool of database connections using HikariCP.
 */
public class ConnectionPool {
    private final HikariDataSource dataSource;

    /**
     * Creates a new connection pool with the specified configuration.
     *
     * @param config HikariCP configuration
     */
    public ConnectionPool(HikariConfig config) {
        try {
            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DB2HelperException("Failed to initialize connection pool", e);
        }
    }

    /**
     * Gets a connection from the pool.
     *
     * @return A database connection
     * @throws DB2HelperException if unable to get a connection
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to get connection from pool", e);
        }
    }

    /**
     * Gets the underlying data source.
     *
     * @return The HikariCP data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Closes the connection pool and releases all resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
} 