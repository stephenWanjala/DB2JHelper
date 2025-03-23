# **dbjhelper-ktx**
Kotlin extensions and coroutine support for `dbjhelper`, simplifying database access with idiomatic Kotlin.

---

## **Features**
- 🏎 **Kotlin DSL for Query Building** – Write SQL queries fluently with `QueryBuilder.kt`
- 🔄 **Coroutine Support** – Perform database operations asynchronously using Kotlin coroutines.
- ⚡ **Reactive Programming** – Support for `Flow` to handle streaming database results.
- 🛠 **Convenient Extensions** – Extensions for `ResultSet` and `Connection` to make DB access easier.
- 🎯 **Model Mapping** – Easily map `ResultSet` rows to Kotlin data classes.

---

## **Installation**
Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.stephenwanjala:dbjhelper-ktx:2.0.0")
}
```

or in `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.stephenwanjala:dbjhelper-ktx:2.0.0'
}
```

---

## **Usage**

### **1️⃣ Query Building**
Use `QueryBuilder` to construct SQL queries fluently:

```kotlin
val query = QueryBuilder
    .select("id", "name", "email")
    .from("users")
    .where("age > ?", 18)
    .orderBy("name")
    .limit(10)
    .getQuery()

println(query) 
// Output: SELECT id, name, email FROM users WHERE age > ? ORDER BY name FETCH FIRST 10 ROWS ONLY
```

---

### **2️⃣ Mapping `ResultSet` to Data Class**
Use `toModel()` or `toModelList()` to convert SQL results into Kotlin data classes:

```kotlin
data class User(val id: Int, val name: String, val email: String)

val user: User? = resultSet.toModel<User>()
val users: List<User> = resultSet.toModelList<User>()
```

---

### **3️⃣ Coroutine-Friendly Database Access**
Execute database operations asynchronously using coroutines:

```kotlin
suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
    connection.use { conn ->
        conn.prepareStatement("SELECT * FROM users").use { stmt ->
            stmt.executeQuery().toModelList<User>()
        }
    }
}
```

---

### **4️⃣ Reactive Query Execution (Flow)**
Stream database results using Kotlin's `Flow`:

```kotlin
fun fetchUsersAsFlow(): Flow<User> = connection.executeQueryAsFlow("SELECT * FROM users")
```

---

## **Extensions Included**
| Extension | Description |
|-----------|------------|
| `ResultSet.toModel<T>()` | Maps a single row to a Kotlin data class. |
| `ResultSet.toModelList<T>()` | Maps multiple rows to a list of Kotlin data classes. |
| `Connection.queryAsync()` | Runs a SQL query asynchronously with coroutines. |
| `Connection.executeQueryAsFlow()` | Streams query results using Kotlin `Flow`. |

---

## **License**
This project is licensed under the **Apache License 2.0**.

---

## **Contributing**
Contributions are welcome! Feel free to open issues or submit pull requests. 🚀