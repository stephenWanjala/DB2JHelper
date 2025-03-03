package io.github.stephenWanjala.db2.internal;

import io.github.stephenWanjala.db2.config.DataSourceConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Objects;

class SimpleDataSource implements DataSource {
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
