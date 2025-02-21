package io.github.stephenWanjala.db2.config;


import javax.sql.DataSource;

/**
 * Configuration class for DB2 database connections.
 * <p>
 * Supports both direct driver-based connections and custom DataSource configuration.
 * Use the builder to create instances:
 * <pre>{@code
 * DataSourceConfig config = new DataSourceConfig.Builder()
 *     .url("jdbc:db2://host:50000/db")
 *     .user("admin")
 *     .password("secret")
 *     .build();
 * }</pre>
 */
public class DataSourceConfig {
    private final String url;
    private final String user;
    private final String password;
    private final DataSource dataSource;

    /**
     * Private constructor - use builder to create instances
     */
    private DataSourceConfig(Builder builder) {
        this.url = builder.url;
        this.user = builder.user;
        this.password = builder.password;
        this.dataSource = builder.dataSource;
    }

    /**
     * @return JDBC connection URL if using direct driver configuration
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return Database username if using direct driver configuration
     */
    public String getUser() {
        return user;
    }

    /**
     * @return Database password if using direct driver configuration
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Custom DataSource if configured, null otherwise
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Builder class for DataSourceConfig
     */
    public static class Builder {
        private String url;
        private String user;
        private String password;
        private DataSource dataSource;

        /**
         * Set JDBC connection URL
         * @param url DB2 JDBC URL (e.g., "jdbc:db2://host:port/db")
         * @return this builder
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Set database username
         * @param user Database username
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Set database password
         * @param password Database password
         * @return this builder
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Set custom DataSource (overrides url/user/password)
         * @param dataSource Configured DataSource instance
         * @return this builder
         */
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Build the DataSourceConfig instance
         * @return Configured DataSourceConfig
         * @throws IllegalStateException if neither DataSource nor URL is provided
         */
        public DataSourceConfig build() {
            if (dataSource == null && url == null) {
                throw new IllegalStateException("Must provide either DataSource or JDBC URL");
            }
            return new DataSourceConfig(this);
        }
    }
}