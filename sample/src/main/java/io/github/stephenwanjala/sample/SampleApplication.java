package io.github.stephenwanjala.sample;

import io.github.stephenwanjala.dbjhelper.core.DB2Connection;
import io.github.stephenwanjala.dbjhelper.query.QueryBuilder;
import io.github.stephenwanjala.dbjhelper.query.QueryExecutor;
import io.github.stephenwanjala.sample.model.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SampleApplication {
    public static void main(String[] args) {
        // Get environment variables
        String jdbcUrl = getEnv("DB2_URL", "");
        String username = getEnv("DB2_USER", "");
        String password = getEnv("DB2_PASSWORD", "");

        if (jdbcUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("Missing required parameters");
            return;
        }

        // Create connection properties
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("currentSchema", username.toUpperCase());
        properties.setProperty("connectionTimeout", getEnv("DB2_CONNECTION_TIMEOUT", "30000"));
        properties.setProperty("idleTimeout", getEnv("DB2_IDLE_TIMEOUT", "600000"));
        properties.setProperty("maxLifetime", getEnv("DB2_MAX_LIFETIME", "1800000"));

        try (DB2Connection db2Connection = new DB2Connection(jdbcUrl, properties)) {
            Connection connection = db2Connection.getConnection();
            QueryExecutor executor = new QueryExecutor(connection);

            // Example 1: Using QueryBuilder with static factory methods
            System.out.println("\nExample 1: Using QueryBuilder with static factory methods");
            List<Customer> customers1 = executor.executeQuery(
                    QueryBuilder.select("LEDGER_NAME", "LEDGER_NUMBER")
                            .from("CUSTOMERS")
                            .getQuery(),
                    rs -> {
                        try {
                            return new Customer(
                                    rs.getString("LEDGER_NAME"),
                                    rs.getString("LEDGER_NUMBER")
                            );
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
            );
            System.out.println("Customers1: " + customers1);

            // Example 2: Using direct query method returning maps
            System.out.println("\nExample 2: Using direct query method");
            List<Map<String, Object>> results = executor.query("SELECT LEDGER_NAME, LEDGER_NUMBER FROM CUSTOMERS");
            System.out.println("\nResults: " + results);

            // Example 3: Query with parameters and automatic type mapping
            System.out.println("\nExample 3: Query with parameters and type mapping");
            Customer customer = executor.queryForObject(
                    "SELECT LEDGER_NAME, LEDGER_NUMBER FROM CUSTOMERS WHERE LEDGER_NUMBER = ?",
                    Customer.class,
                    "HESA"
            );
            System.out.println("Customer: " + customer);

            // Example 4: Transaction example
            System.out.println("\nExample 4: Transaction example");
            executor.transaction(tx -> {
                // Update customer
                int updated = tx.executeUpdate(
                        QueryBuilder.update("CUSTOMERS")
                                .set("LEDGER_NAME = ?", "Updated Name")
                                .where("LEDGER_NUMBER = ?", "HESA")
                                .getQuery(),
                        "Updated Name", "HESA"
                );

                // Insert new customer
                int inserted = tx.executeUpdate(
                        QueryBuilder.insert("CUSTOMERS", "LEDGER_NAME", "LEDGER_NUMBER")
                                .getQuery(),
                        "New Customer", "NEW001"
                );

                return updated + inserted; // Return total affected rows
            });

            System.out.println("The Updated Customer\n");
            Customer customerUpdate = executor.queryForObject(
                    "SELECT LEDGER_NAME, LEDGER_NUMBER FROM CUSTOMERS WHERE LEDGER_NUMBER = ?",
                    Customer.class,
                    "HESA"
            );
            System.out.println("CustomerUpdated: " + customerUpdate);

            System.out.println("\nInserted Customer");
            Customer customerInserted = executor.queryForObject(
                    "SELECT LEDGER_NAME, LEDGER_NUMBER FROM CUSTOMERS WHERE LEDGER_NUMBER = ?",
                    Customer.class,
                    "NEW001"
            );
            System.out.println("CustomerInserted: " + customerInserted);


            // Example 5: Batch insert example
            System.out.println("\nExample 5: Batch insert example");
            List<Object[]> batchParams = Arrays.asList(
                    new Object[]{"Batch Customer 1", "BATCH001"},
                    new Object[]{"Batch Customer 2", "BATCH002"}
            );

            int[] batchResults = executor.executeBatch(
                    QueryBuilder.insert("CUSTOMERS", "LEDGER_NAME", "LEDGER_NUMBER")
                            .getQuery(),
                    batchParams
            );
            System.out.println("Batch results: " + Arrays.toString(batchResults));

            // Example 6: Complex query with multiple conditions
            System.out.println("\nExample 6: Complex query");
            List<Customer> customers2 = executor.executeQuery(
                    QueryBuilder.select("LEDGER_NAME", "LEDGER_NUMBER")
                            .from("CUSTOMERS")
                            .where("LEDGER_NAME LIKE ?", "%Customer%")
                            .or("LEDGER_NUMBER LIKE ?", "BATCH%")
                            .orderBy("LEDGER_NAME")
                            .limit(5)
                            .getQuery(),
                    rs -> {
                        try {
                            return new Customer(
                                    rs.getString("LEDGER_NAME"),
                                    rs.getString("LEDGER_NUMBER")
                            );
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    },
                    "%Customer%", "BATCH%"
            );
            System.out.println("Filtered customers: " + customers2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
