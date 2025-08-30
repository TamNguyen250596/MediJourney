package com.example.medijourney.common.managers.fire_store

import com.google.firebase.firestore.Filter

class FSFilterBuilder {
    private val filters = mutableListOf<Filter>()

    fun equalTo(key: String, value: Any): FSFilterBuilder {
        val filter = Filter.equalTo(key, value)
        filters.add(filter)
        return this
    }

    fun build(): List<Filter> = filters.toList()
}