package com.example.medijourney.common.managers.fire_store

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query

class FSQueryBuilder {

    // Properties
    private val filters = mutableListOf<Filter>()
    private val orders = mutableListOf<Pair<String, Query.Direction>>()
    private var limit: Long? = null

    // Functions
    fun equalTo(key: String, value: Any?): FSQueryBuilder {
        val filter = Filter.equalTo(key, value)
        filters.add(filter)
        return this
    }

    fun lessThan(key: String, value: Any): FSQueryBuilder {
        val filter = Filter.lessThan(key, value)
        filters.add(filter)
        return this
    }

    fun greaterThan(key: String, value: Any): FSQueryBuilder {
        val filter = Filter.greaterThan(key, value)
        filters.add(filter)
        return this
    }

    fun inValues(key: String, values: List<Any>): FSQueryBuilder {
        val filter = Filter.inArray(key, values)
        filters.add(filter)
        return this

    }

    fun arrayContains(key: String, value: Any): FSQueryBuilder {
        val filter = Filter.arrayContains(key, value)
        filters.add(filter)
        return this
    }

    fun orderBy(key: String, direction: Query.Direction): FSQueryBuilder {
        orders.add(Pair(key, direction))
        return this
    }

    fun limit(limit: Long): FSQueryBuilder {
        this.limit = limit
        return this
    }

    fun buildFilter(): List<Filter> = filters.toList()
    fun buildOrders(): List<Pair<String, Query.Direction>> = orders.toList()
    fun buildLimit(): Long? = limit
}