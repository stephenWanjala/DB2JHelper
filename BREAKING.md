# Breaking Changes in DB2JHelper 2.0.0

This document outlines the breaking changes introduced in version 2.0.0 of DB2JHelper and provides migration guidance.

## Major Breaking Changes

### 1. Client Initialization
**Before (1.x):**
```java
DB2Client client = new DB2Client(dataSource);
```
**Now (2.0.0):**
```java
DB2Connection connection = new DB2Connection(jdbcUrl, properties);
QueryExecutor executor = new QueryExecutor(connection.getConnection());
```

### 2. Query Building
**Before (1.x):**
```java
client.select("LEDGER_NAME", "LEDGER_NUMBER")
      .from("CUSTOMERS")
      .list();
```
**Now (2.0.0):**
```java
executor.executeQuery(
    QueryBuilder.select("LEDGER_NAME", "LEDGER_NUMBER")
              .from("CUSTOMERS")
              .getQuery(),
    resultSetMapper
);
```

### 3. Direct Query Execution
**Before (1.x):**
```java
List<Map<String, Object>> results = client.query(sql);
```
**Now (2.0.0):**
```java
List<Map<String, Object>> results = executor.query(sql);
```

### 4. Object Mapping
**Before (1.x):**
```java
Customer customer = client.queryForObject(sql, Customer.class, "HESA");
```
**Now (2.0.0):**
```java
// Option 1: Using queryForObject with class (requires no-args constructor and setters)
Customer customer = executor.queryForObject(sql, Customer.class, "HESA");

// Option 2: Using explicit mapping
Customer customer = executor.executeQuery(sql, 
    rs -> new Customer(rs.getString("LEDGER_NAME"), rs.getString("LEDGER_NUMBER")), 
    "HESA"
).get(0);
```

### 5. Transaction Handling
**Before (1.x):**
```java
client.transaction(conn -> {
    client.update("UPDATE accounts SET balance = balance - ?", 1000);
    client.update("UPDATE payments SET status = 'PROCESSED'");
    return "Transaction completed";
});
```
**Now (2.0.0):**
```java
executor.transaction(tx -> {
    tx.executeUpdate("UPDATE accounts SET balance = balance - ?", 1000);
    tx.executeUpdate("UPDATE payments SET status = 'PROCESSED'");
    return "Transaction completed";
});
```

### 6. Resource Management
**Before (1.x):**
- Automatic resource management handled by DB2Client

**Now (2.0.0):**
- Must explicitly use try-with-resources for both DB2Connection and QueryExecutor
```java
try (DB2Connection connection = new DB2Connection(jdbcUrl, properties);
     QueryExecutor executor = new QueryExecutor(connection.getConnection())) {
    // Use executor
}
```

### 7. Configuration
**Before (1.x):**
```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl(URL);
// ... other config
dataSource = new HikariDataSource(config);
```
**Now (2.0.0):**
```java
Properties properties = new Properties();
properties.setProperty("user", username);
// ... other properties
DB2Connection connection = new DB2Connection(jdbcUrl, properties);
```

## Migration Guide

### Step 1: Update Dependencies
Update your dependency to the new version:

```kotlin
// Gradle (Kotlin DSL)
implementation("io.github.stephenWanjala:DB2JHelper:2.0.0")
```
```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.stephenWanjala</groupId>
    <artifactId>DB2JHelper</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Step 2: Update Your Code

1. Replace DB2Client Usage:
   - Remove all direct DB2Client usage
   - Introduce DB2Connection and QueryExecutor
   - Update resource management with try-with-resources

2. Update Query Building:
   - Use QueryBuilder static methods
   - Add explicit result mapping
   - Update transaction blocks

3. Update Object Mapping:
   - Either add no-args constructors and setters for automatic mapping
   - Or implement explicit ResultSet mapping functions

### Step 3: Error Handling
Update your error handling as exceptions are now wrapped in `DB2HelperException`:

```java
try {
    // DB2JHelper operations
} catch (DB2HelperException e) {
    // Handle database errors
    logger.error("Database error: " + e.getMessage(), e);
}
```

## Benefits of the New API

1. **Better Separation of Concerns**
   - Clear distinction between connection management and query execution
   - More modular and maintainable code

2. **Enhanced Type Safety**
   - Improved compile-time checking
   - More explicit error handling

3. **More Flexible Object Mapping**
   - Support for both automatic and manual mapping
   - Better control over object creation

4. **Improved Resource Management**
   - Explicit resource handling
   - Better connection lifecycle management

5. **Enhanced Query Building**
   - More intuitive query construction
   - Better support for complex queries

6. **Better Transaction Control**
   - More explicit transaction boundaries
   - Improved error handling in transactions

## Example Migration

### Before (1.x):
```java
DB2Client client = new DB2Client(dataSource);
List<Customer> customers = client
    .select("LEDGER_NAME", "LEDGER_NUMBER")
    .from("CUSTOMERS")
    .where("LEDGER_NUMBER = ?", "HESA")
    .list();
```

### After (2.0.0):
```java
try (DB2Connection connection = new DB2Connection(jdbcUrl, properties);
     QueryExecutor executor = new QueryExecutor(connection.getConnection())) {
    
    List<Customer> customers = executor.executeQuery(
        QueryBuilder.select("LEDGER_NAME", "LEDGER_NUMBER")
                  .from("CUSTOMERS")
                  .where("LEDGER_NUMBER = ?", "HESA")
                  .getQuery(),
        rs -> new Customer(
            rs.getString("LEDGER_NAME"),
            rs.getString("LEDGER_NUMBER")
        ),
        "HESA"
    );
}
```

## Need Help?

If you encounter any issues during migration:
1. Check the [sample module](sample) for comprehensive examples
2. Open an issue on GitHub
3. Refer to the updated documentation
4. Contact the maintainers 