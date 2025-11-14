package com.example.medijourney.common.extensions

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T : RealmObject> RealmQuery<T>.toFlow(keyPath: List<String>? = null): Flow<List<T>> {
    return this.asFlow(keyPath).map { it.list }
}

class RQueryBuilder {

    /**
     * Represents a single filter condition in the Realm query.
     *
     * @param query The Realm query string expression (e.g., "age > $0").
     * @param args The arguments to substitute into the query placeholders.
     */
    data class Filter(val query: String, val args: List<Any?>)

    private val filters: MutableList<Filter> = mutableListOf()
    private val sorts: MutableList<Pair<String, Sort>> = mutableListOf()

    /**
     * Filters the query results to include only objects where the specified field
     * is equal to the given value.
     */
    fun equalTo(key: String, value: Any?): RQueryBuilder {
        filters.add(Filter("$key == $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * is not equal to the given value.
     */
    fun notEqualTo(key: String, value: Any): RQueryBuilder {
        filters.add(Filter("$key != $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * is greater than the given value.
     */
    fun greaterThan(key: String, value: Any): RQueryBuilder {
        filters.add(Filter("$key > $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * is greater than or equal to the given value.
     */
    fun greaterThanOrEqualTo(key: String, value: Any): RQueryBuilder {
        filters.add(Filter("$key >= $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * is less than the given value.
     */
    fun lessThan(key: String, value: Any): RQueryBuilder {
        filters.add(Filter("$key < $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * is less than or equal to the given value.
     */
    fun lessThanOrEqualTo(key: String, value: Any): RQueryBuilder {
        filters.add(Filter("$key <= $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified string field
     * contains the given substring.
     *
     * @param caseSensitive Whether the comparison is case-sensitive (default: true).
     */
    fun contains(key: String, value: String, caseSensitive: Boolean = true): RQueryBuilder {
        val modifier = if (caseSensitive) "" else "[c]"
        filters.add(Filter("$key CONTAINS$modifier $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified string field
     * begins with the given prefix.
     *
     * @param caseSensitive Whether the comparison is case-sensitive (default: true).
     */
    fun beginsWith(key: String, value: String, caseSensitive: Boolean = true): RQueryBuilder {
        val modifier = if (caseSensitive) "" else "[c]"
        filters.add(Filter("$key BEGINSWITH$modifier $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified string field
     * ends with the given suffix.
     *
     * @param caseSensitive Whether the comparison is case-sensitive (default: true).
     */
    fun endsWith(key: String, value: String, caseSensitive: Boolean = true): RQueryBuilder {
        val modifier = if (caseSensitive) "" else "[c]"
        filters.add(Filter("$key ENDSWITH$modifier $0", listOf(value)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * matches one of the given values.
     */
    fun inValues(key: String, values: List<Any>): RQueryBuilder {
        filters.add(Filter("$key IN $0", listOf(values)))
        return this
    }

    /**
     * Filters the query results to include only objects where the specified field
     * falls between two values (inclusive).
     */
    fun between(key: String, from: Any, to: Any): RQueryBuilder {
        filters.add(Filter("$key BETWEEN { $0, $1 }", listOf(from, to)))
        return this
    }

    fun sort(key: String, sortOrder: Sort): RQueryBuilder {
        sorts.add(Pair(key, sortOrder))
        return this
    }

    fun sort(key: String, isAscending: Boolean): RQueryBuilder {
        val sortOrder = if (isAscending) Sort.ASCENDING else Sort.DESCENDING
        sorts.add(Pair(key, sortOrder))
        return this
    }

    /**
     * Applies all accumulated filters to the provided RealmQuery.
     *
     * @param query The initial RealmQuery to apply filters to.
     * @return The updated RealmQuery with all filter conditions applied.
     */
    fun <T : RealmObject> applyTo(query: RealmQuery<T>): RealmQuery<T> {
        var q = query
        for (filter in filters) {
            q = q.query(filter.query, *filter.args.toTypedArray())
        }
        for (sort in sorts) {
            q = q.sort(sort.first, sort.second)
        }
        return q
    }
}