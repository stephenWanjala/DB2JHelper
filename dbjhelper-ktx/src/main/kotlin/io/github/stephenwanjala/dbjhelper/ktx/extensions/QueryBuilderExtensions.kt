package io.github.stephenWanjala.io.github.stephenwanjala.dbjhelper.ktx.extensions

import io.github.stephenwanjala.dbjhelper.query.QueryBuilder

fun QueryBuilder.whereIf(condition: Boolean, clause: String, vararg params: Any): QueryBuilder {
    return if (condition) this.where(clause, *params) else this
}

fun QueryBuilder.limitIf(condition: Boolean, limit: Int): QueryBuilder {
    return if (condition) this.limit(limit) else this
}
