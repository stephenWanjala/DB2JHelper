# DB2JHelper

### **Simplified DB2 Database Operations for Java**
_A lightweight, modern Java library for effortless DB2 database interactions_

**Overview**

>I was kinda tired Of wring this Kinda of Code every time When I have To spin Up Any new Module For Fx Apps Using DB2 soo Yeah 
> 
DB2JHelper is a developer-friendly Java library designed to streamline DB2 database operations while maintaining performance and safety. It abstracts JDBC boilerplate and provides intuitive APIs for common database tasks, allowing developers to focus on business logic rather than database plumbing.

## Key Features

### Feature Description

| **Feature**              | **Description**                                                               |
|--------------------------|-------------------------------------------------------------------------------|
| **Fluent Query Builder** | Chainable API for SQL construction: `client.select().from().where().list()`   |
| **Smart CRUD**           | Simplified insert/update/delete with automatic parameter binding              |
| **POJO Mapping**         | Map query results to Java objects via lambda expressions                      |
| **Batch Processing**     | Optimized bulk operations with JDBC batch support                             |
| **Transaction Control**  | Declarative transactions with configurable isolation levels and auto-rollback |
| **Connection Pooling**   | Built-in support for connection pooling (HikariCP-ready)                      |
| **Runtime Safety**       | Automatic resource cleanup and unified exception handling                     |
| **DB2-Specific**         | Optimized for DB2 SQL dialect and features                                    |

### Why DB2JHelper?

✅ Reduce Boilerplate - 80% less JDBC code compared to raw implementations  
✅ Type-Safe Operations - Compile-time query validation through fluent API  
✅ Modern Java - Leverages lambda expressions and functional programming  
✅ Production Ready - Connection pooling, transaction recovery, and error logging  
✅ Lightweight - Minimal dependencies (JDBC driver + optional HikariCP)
#### Quick Start
> Maven Deps Coming Soon ---
### Basic Usage:
````java


// Configure
DataSourceConfig config = new DataSourceConfig.Builder()
.url("jdbc:db2://localhost:50000/SAMPLE")
.user("db2admin")
.password("securepass")
.build();
````
### Basic Query:
```java

List<Map<String, Object>> results = client
        .query("SELECT * FROM users WHERE age > ?", 25);
```

### Query with fluent API
````java
try (DB2Client client = new DB2Client(config)) {
List<Employee> employees = client
        .select("id", "name", "salary")
        .from("employees")
        .where("department = ? AND salary > ?", "Engineering", 75000)
        .map(rs -> new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("salary")
        ));
}

````
**or**
````java
List<Map<String, Object>> employees = client.select("id", "name")
    .from("employees")
    .where("department = ?", "Engineering")
    .orderBy("name DESC")
    .list();
````

### Transaction example
`````java
client.transaction(conn -> {
client.update("UPDATE accounts SET balance = balance - ?", 1000);
client.update("UPDATE payments SET status = 'PROCESSED'");
return "Transaction completed";
});

``````

## Use Cases

* Rapid development of DB2-backed Java applications
* 
* Migration from legacy JDBC code to modern patterns
* 
* Batch processing of financial/transactional data
* 
* Microservices requiring lightweight database access
* 
* Prototyping with quick SQL-to-POJO mapping