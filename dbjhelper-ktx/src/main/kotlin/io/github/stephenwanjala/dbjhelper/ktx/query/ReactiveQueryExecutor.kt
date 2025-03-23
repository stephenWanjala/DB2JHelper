package io.github.stephenwanjala.dbjhelper.ktx.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

object ReactiveQueryExecutor {
    fun executeQueryFlow(connection: Connection, sql: String, vararg params: Any?): Flow<ResultSet> = flow {
        connection.prepareStatement(sql).use { statement ->
            params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                emit(resultSet)
            }
        }
    }
}
