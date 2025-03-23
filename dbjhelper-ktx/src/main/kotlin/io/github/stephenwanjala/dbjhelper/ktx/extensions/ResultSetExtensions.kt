package io.github.stephenwanjala.dbjhelper.ktx.extensions

import io.github.stephenwanjala.dbjhelper.ktx.query.ModelMapperKtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

fun ResultSet.getIntOrNull(column: String): Int? = getObject(column)?.let { (it as? Number)?.toInt() }
fun ResultSet.getLongOrNull(column: String): Long? = getObject(column)?.let { (it as? Number)?.toLong() }
fun ResultSet.getStringOrNull(column: String): String? = getObject(column) as? String
fun ResultSet.getBooleanOrNull(column: String): Boolean? = getObject(column)?.let {
    (it as? Boolean ?: (it as? Number)?.toInt()) == 1
}


/**
 * Extension function to map the current row of a [ResultSet] to a Kotlin data class.
 * @return An instance of the model populated with data from the current row or `null` if no data is available.
 */
suspend inline fun <reified T : Any> ResultSet.toModel(): T? = withContext(Dispatchers.IO) {
    ModelMapperKtx.map(this@toModel)
}

/**
 * Extension function to map all rows of a [ResultSet] to a list of Kotlin data class instances.
 * @return A list of mapped model objects.
 */
suspend inline fun <reified T : Any> ResultSet.toModelList(): List<T> = withContext(Dispatchers.IO) {
    ModelMapperKtx.mapAll(this@toModelList)
}
