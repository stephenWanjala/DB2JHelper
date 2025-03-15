# DB2JHelper

### **Simplified DB2 Database Operations for Java**
_A lightweight, modern Java library for effortless DB2 database interactions_

## Overview

DB2JHelper is a developer-friendly Java library designed to streamline DB2 database operations while maintaining performance and safety. It abstracts JDBC boilerplate and provides intuitive APIs for common database tasks, allowing developers to focus on business logic rather than database plumbing.

## Installation

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.github.stephenWanjala:DB2JHelper:1.0.0")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.stephenWanjala</groupId>
        <artifactId>DB2JHelper</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Key Features

| **Feature**              | **Description**                                                               |
|--------------------------|-------------------------------------------------------------------------------|
| **Connection Pooling**   | Built-in HikariCP integration for efficient connection management              |
| **Query Builder**        | Type-safe SQL query construction with parameter binding                        |
| **Exception Handling**   | Unified exception hierarchy with meaningful error messages                     |
| **Utility Functions**    | Helper methods for common DB2 operations                                       |
| **Resource Management**  | Automatic cleanup of database resources                                        |
| **Transaction Support**  | Simple transaction management with auto-rollback                               |

## Quick Start

### 1. Basic Connection
```java
String jdbcUrl = DB2Utils.buildJdbcUrl("localhost", 50000, "SAMPLE");
try (DB2Connection connection = new DB2Connection(jdbcUrl, "username", "password")) {
    // Use the connection
}
```

### 2. Query Execution
```java
QueryExecutor executor = new QueryExecutor(connection);

// Simple query
List<Employee> employees = executor.executeQuery(
    "SELECT * FROM employees WHERE department = ?",
    rs -> new Employee(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("department"),
        rs.getDouble("salary")
    ),
    "Engineering"
);
```

### 3. Query Builder Usage
```java
QueryBuilder queryBuilder = new QueryBuilder("SELECT * FROM employees");
queryBuilder.where("salary > ?", 50000.00)
           .and("department = ?", "Engineering")
           .orderBy("name");

List<Employee> employees = executor.executeQuery(
    queryBuilder.getQuery(),
    rs -> new Employee(/* mapping */),
    queryBuilder.getParameters().toArray()
);
```

### 4. Batch Operations
```java
List<Object[]> batchParams = Arrays.asList(
    new Object[]{"John Doe", "Engineering", 85000.00},
    new Object[]{"Jane Smith", "Marketing", 75000.00}
);

int[] results = executor.executeBatch(
    "INSERT INTO employees (name, department, salary) VALUES (?, ?, ?)",
    batchParams
);
```

### 5. Utility Functions
```java
// List all tables
List<String> tables = DB2Utils.listTables(connection);

// Check if table exists
boolean exists = DB2Utils.tableExists(connection, "employees");

// Get column information
List<String> columns = DB2Utils.listColumns(connection, "employees");
```

## Best Practices

1. **Resource Management**
   - Always use try-with-resources for connections
   - Close resources explicitly when not using try-with-resources

2. **Error Handling**
   - Catch `DB2HelperException` for library-specific errors
   - Use appropriate logging in catch blocks

3. **Connection Pooling**
   - Reuse DB2Connection instances
   - Configure pool settings based on your application needs

## Sample Application

Check the [sample](sample) module for a complete working example demonstrating:
- Connection setup
- Table creation
- Data insertion
- Query execution
- Result mapping
- Utility function usage

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

[![](https://jitpack.io/v/stephenWanjala/DB2JHelper.svg)](https://jitpack.io/#stephenWanjala/DB2JHelper)