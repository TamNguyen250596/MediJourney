package com.example.medijourney.common.managers.realm

import io.realm.kotlin.types.RealmObject

interface RealmCycle {
    fun primaryKey(): String
    fun toRealmObject(map: Map<String, Any>): RealmObject
    fun updateFromMap(map: Map<String, Any>)
    fun handleDependencies(map: Map<String, Any>) {}
    fun removeDependencies() {}
}

sealed class RQuery {
    data class Where(val field: String, val operator: Operator, val value: Any) : RQuery()
    data class And(val queries: List<RQuery>) : RQuery()
    data class Or(val queries: List<RQuery>) : RQuery()
}

enum class Operator(val value: String) {
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    IN("in"),
    NOT_IN("not in"),
    CONTAINS("CONTAINS")
}

fun where(field: String, operator: Operator, value: Any): RQuery {
    return RQuery.Where(field, operator, value)
}

fun and(vararg queries: RQuery): RQuery {
    return RQuery.And(queries.toList())
}

fun or(vararg queries: RQuery): RQuery {
    return RQuery.Or(queries.toList())
}