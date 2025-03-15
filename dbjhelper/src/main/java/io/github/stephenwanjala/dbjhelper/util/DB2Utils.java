package io.github.stephenwanjala.dbjhelper.util;

import io.github.stephenwanjala.dbjhelper.exception.DB2HelperException;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for working with DB2 databases.
 */
public class DB2Utils {
    
    private DB2Utils() {
        // Utility class, no instantiation
    }

    /**
     * Builds a DB2 JDBC URL from components.
     *
     * @param host The database host
     * @param port The database port
     * @param database The database name
     * @return The JDBC URL
     */
    public static String buildJdbcUrl(@Nonnull String host, int port, @Nonnull String database) {
        return String.format("jdbc:db2://%s:%d/%s", host, port, database);
    }

    /**
     * Gets a list of all tables in the current schema.
     *
     * @param connection The database connection
     * @return List of table names
     * @throws DB2HelperException if the operation fails
     */
    public static List<String> listTables(@Nonnull Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> tables = new ArrayList<>();
            
            try (ResultSet rs = metaData.getTables(
                    connection.getCatalog(), 
                    connection.getSchema(),
                    "%",
                    new String[]{"TABLE"})) {
                        
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to list tables", e);
        }
    }

    /**
     * Gets the columns for a specific table.
     *
     * @param connection The database connection
     * @param tableName The name of the table
     * @return List of column names
     * @throws DB2HelperException if the operation fails
     */
    public static List<String> listColumns(@Nonnull Connection connection, @Nonnull String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> columns = new ArrayList<>();
            
            try (ResultSet rs = metaData.getColumns(
                    connection.getCatalog(),
                    connection.getSchema(),
                    tableName,
                    "%")) {
                        
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
            return columns;
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to list columns for table: " + tableName, e);
        }
    }

    /**
     * Checks if a table exists in the current schema.
     *
     * @param connection The database connection
     * @param tableName The name of the table to check
     * @return true if the table exists, false otherwise
     * @throws DB2HelperException if the operation fails
     */
    public static boolean tableExists(@Nonnull Connection connection, @Nonnull String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet rs = metaData.getTables(
                    connection.getCatalog(),
                    connection.getSchema(),
                    tableName,
                    new String[]{"TABLE"})) {
                        
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DB2HelperException("Failed to check if table exists: " + tableName, e);
        }
    }
} 