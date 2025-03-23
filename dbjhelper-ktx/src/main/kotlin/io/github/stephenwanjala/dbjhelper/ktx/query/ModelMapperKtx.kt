package io.github.stephenwanjala.dbjhelper.ktx.query

import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ModelMapperKtx {
    inline fun <reified T : Any> map(resultSet: ResultSet): T? {
        val clazz = T::class
        return if (resultSet.next()) mapRow(resultSet, clazz) else null
    }

    inline fun <reified T : Any> mapAll(resultSet: ResultSet): List<T> {
        val clazz = T::class
        val list = mutableListOf<T>()
        while (resultSet.next()) {
            mapRow(resultSet, clazz)?.let { list.add(it) }
        }
        return list
    }

    fun <T : Any> mapRow(resultSet: ResultSet, clazz: KClass<T>): T? {
        val constructor = clazz.primaryConstructor ?: return null
        val args = constructor.parameters.associateWith { param ->
            val columnName = param.name?.replace(Regex("([A-Z])"), "_$1")?.lowercase()
            val value = resultSet.getObject(columnName)
            convert(value, param.type.classifier as? KClass<*>)
        }
        return constructor.callBy(args)
    }

    private fun convert(value: Any?, targetType: KClass<*>?): Any? {
        return when (targetType) {
            String::class -> value?.toString()
            Int::class -> (value as? Number)?.toInt()
            Long::class -> (value as? Number)?.toLong()
            Double::class -> (value as? Number)?.toDouble()
            Boolean::class -> (value as? Boolean ?: (value as? Number)?.toInt()) == 1
            BigDecimal::class -> value as? BigDecimal ?: value?.toString()?.toBigDecimalOrNull()
            Timestamp::class -> value as? Timestamp
            else -> value
        }
    }
}
