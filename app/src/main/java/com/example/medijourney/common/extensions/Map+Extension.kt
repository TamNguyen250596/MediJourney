package com.example.medijourney.common.extensions

import com.example.medijourney.common.managers.InternationManager
import io.realm.kotlin.types.RealmInstant

fun Map<String, Any>.getInt(key: String, defaultValue: Int? = null): Int {
    return (this[key] as? Number)?.toInt() ?: (defaultValue ?: 0)
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.getIntList(key: String, defaultValue: List<Int>? = null): List<Int> {
    return (this[key] as? List<Number>)?.map { it.toInt() } ?: (defaultValue ?: emptyList())
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.getIntSet(key: String, defaultValue: Set<Int>? = null): Set<Int> {
    return (this[key] as? List<Number>)?.map { it.toInt() }?.toSet()
        ?: (defaultValue?.toSet() ?: emptySet())
}

fun Map<String, Any>.getIntMap(key: String, defaultValue: Map<String, Int>? = null): Map<String, Int> {
    return (this[key] as? Map<*, *>)?.mapNotNull {
        val stringKey = it.key as? String
        val intValue = (it.value as? Number)?.toInt()
        if (stringKey != null && intValue != null) stringKey to intValue else null
    }?.toMap() ?: defaultValue ?: emptyMap()
}

fun Map<String, Any>.getUserObjectKey(defaultKey: String): String {
    return this["id"] as? String ?: this["doc_ref"] as? String ?: defaultKey
}

fun Map<String, Any>.getRealmInstant(key: String, defaultValue: RealmInstant? = null): RealmInstant? {
    val timestamp = (this[key] as? Number)?.toLong()
    return if (timestamp != null && timestamp != 0.toLong()) {
        val seconds = timestamp / 1000
        val nanoseconds = (timestamp % 1000) * 1_000_000
        RealmInstant.from(seconds, nanoseconds.toInt())
    } else {
        defaultValue
    }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.getLocalizedString(key: String, defaultValue: String? = null): String? {
    val localizedMap = this[key] as? Map<String, String>
    return localizedMap?.get(InternationManager.currentLanguageCode) ?: localizedMap?.get(
        InternationManager.currentLocale
    ) ?: defaultValue
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.getStringSet(key: String, defaultValue: Set<String>? = null): Set<String> {
    return (this[key] as? List<String>)?.toSet()
        ?: (defaultValue ?: emptySet())
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.getStringList(key: String, defaultValue: List<String>? = null): List<String> {
    return (this[key] as? List<String>) ?: (defaultValue ?: emptyList())
}

fun Map<String, Any>.getDouble(key: String, defaultValue: Double? = null): Double {
    return (this[key] as? Number)?.toDouble() ?: (defaultValue ?: 0.0)
}