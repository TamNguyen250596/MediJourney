package com.example.medijourney.common.extensions

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Filters the query results to include only objects where the specified field
 * is equal to the given value.
 *
 * @param key The name of the field to compare.
 * @param value The value to match against.
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.equalTo(key: String, value: Any?): RealmQuery<T> {
    return this.query("$key == $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * is not equal to the given value.
 *
 * @param key The name of the field to compare.
 * @param value The value that should not match.
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.notEqualTo(key: String, value: Any): RealmQuery<T> {
    return this.query("$key != $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * is greater than the given value.
 *
 * @param key The name of the field to compare.
 * @param value The lower bound value (exclusive).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.greaterThan(key: String, value: Any): RealmQuery<T> {
    return this.query("$key > $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * is greater than or equal to the given value.
 *
 * @param key The name of the field to compare.
 * @param value The lower bound value (inclusive).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.greaterThanOrEqualTo(key: String, value: Any): RealmQuery<T> {
    return this.query("$key >= $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * is less than the given value.
 *
 * @param key The name of the field to compare.
 * @param value The upper bound value (exclusive).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.lessThan(key: String, value: Any): RealmQuery<T> {
    return this.query("$key < $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * is less than or equal to the given value.
 *
 * @param key The name of the field to compare.
 * @param value The upper bound value (inclusive).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.lessThanOrEqualTo(key: String, value: Any): RealmQuery<T> {
    return this.query("$key <= $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * contains the given substring.
 *
 * @param key The name of the string field to search.
 * @param value The substring to look for.
 * @param caseSensitive Whether the comparison is case-sensitive (default: true).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.contains(
    key: String,
    value: String,
    caseSensitive: Boolean = true
): RealmQuery<T> {
    val modifier = if (caseSensitive) "" else "[c]"
    return this.query("$key CONTAINS$modifier $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * begins with the given prefix.
 *
 * @param key The name of the string field to search.
 * @param value The prefix to match.
 * @param caseSensitive Whether the comparison is case-sensitive (default: true).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.beginsWith(
    key: String,
    value: String,
    caseSensitive: Boolean = true
): RealmQuery<T> {
    val modifier = if (caseSensitive) "" else "[c]"
    return this.query("$key BEGINSWITH$modifier $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * ends with the given suffix.
 *
 * @param key The name of the string field to search.
 * @param value The suffix to match.
 * @param caseSensitive Whether the comparison is case-sensitive (default: true).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.endsWith(
    key: String,
    value: String,
    caseSensitive: Boolean = true
): RealmQuery<T> {
    val modifier = if (caseSensitive) "" else "[c]"
    return this.query("$key ENDSWITH$modifier $0", value)
}

/**
 * Filters the query results to include only objects where the specified field
 * matches one of the given values.
 *
 * @param key The name of the field to compare.
 * @param values The list of allowed values.
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.inValues(key: String, values: List<Any>): RealmQuery<T> {
    return this.query("$key IN $0", values)
}

/**
 * Filters the query results to include only objects where the specified field
 * falls between two values (inclusive).
 *
 * @param key The name of the field to compare.
 * @param from The lower bound value (inclusive).
 * @param to The upper bound value (inclusive).
 * @return The updated [RealmQuery] instance.
 */
fun <T : RealmObject> RealmQuery<T>.between(key: String, from: Any, to: Any): RealmQuery<T> {
    return this.query("$key BETWEEN { $0, $1 }", from, to)
}

/**
 * Converts the RealmQuery to a Flow that emits a list of RealmObjects.
 * This is a convenience function that wraps the `asFlow` method and maps the `RealmResults` to a `List`.
 *
 * @param keyPath An optional list of key paths to observe for changes.
 *                If null, the entire object will be observed.
 * @return A Flow that emits a list of RealmObjects.
 */
fun <T : RealmObject> RealmQuery<T>.toFlow(keyPath: List<String>? = null): Flow<List<T>> {
    return this.asFlow(keyPath).map { it.list }
}

class RFilter {

    data class Filter(val key: String, val value: Any?)
    private val filters: MutableList<Filter> = mutableListOf()

    /**
     * Filters the query results to include only objects where the specified field
     * is equal to the given value.
     *
     * @param key The name of the field to compare.
     * @param value The value to match against.
     * @return The updated [RealmQuery] instance.
     */
    fun equalTo(key: String, value: Any?): RFilter {
        val filter = Filter("$key == $0", value)
        filters.add(filter)
        return this
    }

    fun build(): List<Filter> {
        return filters
    }
}