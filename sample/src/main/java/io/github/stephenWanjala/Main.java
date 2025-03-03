package io.github.stephenWanjala;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.stephenWanjala.db2.core.DB2Client;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        final HikariDataSource dataSource;

        try {
            // Load the DB2 JDBC driver
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load DB2 JDBC driver", e);
        }

        try {
            final String URL = getEnv("DB2_URL", "");
            if (URL.isEmpty()) {
                throw new RuntimeException("DB2_URL is empty");
            }
            final String USER = getEnv("DB2_USER", "auto");
            final String PASSWORD = getEnv("DB2_PASSWORD", "");
            if (USER.isEmpty() || PASSWORD.isEmpty()) {
                throw new RuntimeException("DB USER or DB _PASSWORD is empty");
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.ibm.db2.jcc.DB2Driver");

            config.setMaximumPoolSize(Integer.parseInt(getEnv("DB2_MAX_POOL_SIZE", "50")));
            config.setMinimumIdle(Integer.parseInt(getEnv("DB2_MIN_IDLE", "10")));
            config.setConnectionTimeout(Integer.parseInt(getEnv("DB2_CONNECTION_TIMEOUT", "30000")));
            config.setIdleTimeout(Integer.parseInt(getEnv("DB2_IDLE_TIMEOUT", "6000000")));
            config.setMaxLifetime(
                    Integer.parseInt(getEnv("DB2_MAX_LIFETIME", "1800000"))); // Reduce
            // maxLifetime
            // to 30
            // minutes
            config.setLeakDetectionThreshold(Integer.parseInt(getEnv("DB2_LEAK_DETECTION_THRESHOLD", "20000")));
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        DB2Client client = new DB2Client(dataSource);
        List<Customer> customers1 =
                client
                        .select("LEDGER_NAME", "LEDGER_NUMBER")
                        .from("CUSTOMERS")
                        .list()
                        .stream()
                        .map(
                                rs ->
                                        new Customer(
                                                (String) rs.get("LEDGER_NAME"), (String) rs.get("LEDGER_NUMBER")))
                        .toList();
        System.out.printf("Customers1: %s\n", customers1);

    /*
    customers 2
     */
        String sql = "SELECT LEDGER_NAME,LEDGER_NUMBER FROM CUSTOMERS";
        List<Customer> customers2 =
                client
                        .query(sql)
                        .stream()
                        .map(
                                row ->
                                        new Customer((String) row.get("LEDGER_NAME"), (String) row.get("LEDGER_NUMBER")))
                        .toList();

        System.out.printf("Customers2: %s\n", customers2);

    /*
   Example 3 params
     */
        String sql2 = "SELECT LEDGER_NAME,LEDGER_NUMBER FROM CUSTOMERS where  LEDGER_NUMBER = ?";
        Map<String, Object> row = client.query(sql2, "HESA").get(0);
        Customer customer =
                new Customer((String) row.get("LEDGER_NAME"), (String) row.get("LEDGER_NUMBER"));

        System.out.printf("Customer: %s\n", customer);

    /*
    Example  4
     */
        Customer customers = client.queryForObject(sql2, Customer.class, "HESA");
        System.out.printf("Customers: %s\n", customers);

        List<Customer> customer4 = client.queryForList(sql2, Customer.class, "HESA");
        System.out.printf("Customer:s %s\n", customer4);

        /*
        LIST CUSTOMERS Example 5

         */
        List<Customer> customesr5 = client.queryForList(sql, Customer.class);
        System.out.printf("Customer: %s\n", customesr5);

        /*
        10 suppliers check norArgs constructor
         */

        String suppliersSql = "SELECT LEDGER_NAME,LEDGER_NUMBER FROM SUPPLIERS FETCH FIRST 10 ROWS ONLY";
        List<Supplier> suppliers =client.queryForList(suppliersSql, Supplier.class);
        System.out.printf("Suppliers: %s\n", suppliers);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

}


