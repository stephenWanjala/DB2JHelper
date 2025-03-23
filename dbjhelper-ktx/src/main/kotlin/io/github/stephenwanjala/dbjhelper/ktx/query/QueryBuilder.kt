package io.github.stephenwanjala.dbjhelper.ktx.query

class QueryBuilder private constructor(private val query: StringBuilder) {
    private val parameters = mutableListOf<Any?>()

    companion object {
        fun select(vararg columns: String) = QueryBuilder(StringBuilder("SELECT ${columns.ifEmpty { arrayOf("*") }.joinToString()}"))
        fun insert(table: String, vararg columns: String): QueryBuilder {
            val placeholders = columns.joinToString { "?" }
            return QueryBuilder(StringBuilder("INSERT INTO $table (${columns.joinToString()}) VALUES ($placeholders)"))
        }
        fun update(table: String) = QueryBuilder(StringBuilder("UPDATE $table SET "))
        fun delete(table: String) = QueryBuilder(StringBuilder("DELETE FROM $table"))
    }

    fun from(table: String) = apply { query.append(" FROM $table") }
    fun where(condition: String, vararg params: Any?) = apply {
        query.append(" WHERE $condition")
        parameters.addAll(params)
    }
    fun and(condition: String, vararg params: Any?) = apply {
        query.append(" AND $condition")
        parameters.addAll(params)
    }
    fun or(condition: String, vararg params: Any?) = apply {
        query.append(" OR $condition")
        parameters.addAll(params)
    }
    fun orderBy(vararg columns: String) = apply { query.append(" ORDER BY ${columns.joinToString()}") }
    fun limit(limit: Int) = apply { query.append(" FETCH FIRST $limit ROWS ONLY") }

    fun buildQuery() = query.toString()
    fun buildParameters() = parameters.toList()
}
