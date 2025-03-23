package io.github.stephenwanjala.dbjhelper.ktx.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet

suspend fun Connection.executeUpdate(sql: String, vararg params: Any?): Int = withContext(Dispatchers.IO) {
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
        statement.executeUpdate()
    }
}

suspend fun Connection.executeQuery(sql: String, vararg params: Any?): ResultSet = withContext(Dispatchers.IO) {
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
        statement.executeQuery()
    }
}
