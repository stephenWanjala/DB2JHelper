package io.github.stephenwanjala.dbjhelper.core;

import com.zaxxer.hikari.HikariConfig;
import io.github.stephenwanjala.dbjhelper.exception.DB2HelperException;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.util.Properties;

/**
 * Manages DB2-specific connection functionality.
 */
public class DB2Connection implements  AutoCloseable {
    private final ConnectionPool connectionPool;
    
    /**
     * Creates a new DB2 connection manager with the specified configuration.
     *
     * @param jdbcUrl The JDBC URL for the DB2 database
     * @param username The database username
     * @param password The database password
     */
    public DB2Connection(@Nonnull String jdbcUrl, @Nonnull String username, @Nonnull String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
        
        // DB2-specific default settings
        config.addDataSourceProperty("currentSchema", username.toUpperCase());
        config.addDataSourceProperty("connectionTimeout", "30000");
        config.addDataSourceProperty("idleTimeout", "600000");
        config.addDataSourceProperty("maxLifetime", "1800000");
        
        this.connectionPool = new ConnectionPool(config);
    }

    /**
     * Creates a new DB2 connection manager with custom properties.
     *
     * @param jdbcUrl The JDBC URL for the DB2 database
     * @param properties Connection properties including username and password
     */
    public DB2Connection(@Nonnull String jdbcUrl, @Nonnull Properties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDataSourceProperties(properties);
        config.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
        
        this.connectionPool = new ConnectionPool(config);
    }

    /**
     * Gets a connection from the pool.
     *
     * @return A database connection
     * @throws DB2HelperException if unable to get a connection
     */
    public Connection getConnection() {
        return connectionPool.getConnection();
    }

    /**
     * Closes all connections and releases resources.
     */
    public void close() {
        connectionPool.close();
    }
} 