package com.example.medijourney.common.managers.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject

object RealmManager {

    // Realm
    fun createRealm(configuration: RealmConfiguration? = null): Realm {
        val config = configuration ?: MRealmConfiguration.config
        return Realm.open(config)
    }

    // Create
    suspend fun <T> create(
        clazz: Class<T>,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val realmObject = realmEntryInstance.toRealmObject(data)
            realmEntryInstance.handleDependencies(data)

            try {
                copyToRealm(realmObject, UpdatePolicy.ERROR)
            } catch (e: Exception) {
                realmEntryInstance.updateFromMap(data)
            }
        }
    }

    suspend fun <T> create(
        clazz: Class<T>,
        dataList: List<Map<String, Any>>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            for (data in dataList) {
                val realmObject = realmEntryInstance.toRealmObject(data)
                realmEntryInstance.handleDependencies(data)

                try {
                    copyToRealm(realmObject, UpdatePolicy.ERROR)
                } catch (e: Exception) {
                    realmEntryInstance.updateFromMap(data)
                }
            }
        }
    }

    // Read
    suspend fun <T> read(
        clazz: Class<T>,
        primaryKey: Any,
        configuration: RealmConfiguration? = null
    ): T? where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        return realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val primaryKeyName = realmEntryInstance.primaryKey()
            realm.query(clazz.kotlin, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
        }
    }

    suspend fun <T : RealmObject> read(
        clazz: Class<T>, realmQuery: RQuery? = null,
        sort: List<Pair<String, Sort>> = listOf(),
        configuration: RealmConfiguration? = null
    ): RealmResults<T> {
        val realm = createRealm(configuration)

        return realm.write {
            var entities = realm.query(clazz.kotlin)
            realmQuery?.let {
                val (filter, args) = buildQuery(realmQuery)
                entities = entities.query(filter, *args.toTypedArray())
            }
            if (sort.isNotEmpty()) {
                sort.forEach { sortOrder ->
                    entities = entities.sort(propertyAndSortOrder = sortOrder)
                }
            }
            entities.find()
        }
    }

    // Update
    suspend fun <T> update(
        clazz: Class<T>,
        primaryKey: Any,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val primaryKeyName = realmEntryInstance.primaryKey()
            val entity = query(clazz.kotlin, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
            entity?.updateFromMap(data)
        }
    }

    suspend fun <T> update(
        entity: T,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            findLatest(entity)?.updateFromMap(data)
        }
    }

    suspend fun <T> update(
        clazz: Class<T>,
        dataList: List<Map<String, Any>>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val primaryKeyName = realmEntryInstance.primaryKey()

            for (data in dataList) {
                val primaryKey = data[primaryKeyName] ?: continue
                val entity = query(clazz.kotlin, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
                entity?.updateFromMap(data)
            }
        }
    }

    // Delete
    suspend fun <T> delete(
        clazz: Class<T>,
        primaryKey: Any,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val primaryKeyName = realmEntryInstance.primaryKey()
            val entity = query(clazz.kotlin, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
            entity?.let {
                it.removeDependencies()
                delete(it)
            }
        }
    }

    suspend fun <T> delete(
        entity: T,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            findLatest(entity)?.let {
                it.removeDependencies()
                delete(it)
            }
        }
    }

    suspend fun <T> delete(
        clazz: Class<T>,
        primaryKeys: List<Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            val realmEntryInstance = clazz.getConstructor().newInstance()
            val primaryKeyName = realmEntryInstance.primaryKey()
            val entities = query(clazz.kotlin, "$primaryKeyName IN $0", primaryKeys).find()
            entities.forEach {
                it.removeDependencies()
            }
            delete(entities)
        }
    }

    suspend fun <T> delete(
        entities: List<T>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            for (entity in entities) {
                findLatest(entity)?.let {
                    it.removeDependencies()
                    delete(it)
                }
            }
        }
    }

    suspend fun <T> delete(
        clazz: Class<T>,
        realmQuery: RQuery? = null,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            var entities = realm.query(clazz.kotlin)
            realmQuery?.let {
                val (filter, args) = buildQuery(realmQuery)
                entities = entities.query(filter, *args.toTypedArray())
            }
            delete(entities.find().onEach { entity ->
                entity.removeDependencies()
            })
        }
    }

    suspend fun deleteAll(
        configuration: RealmConfiguration? = null
    ) {
        val realm = createRealm(configuration)

        realm.write {
            deleteAll()
        }
    }

    // Functions
    private fun buildQuery(realmQuery: RQuery, index: Int = 0): Pair<String, List<Any>> {
        val filter = StringBuilder()
        val arguments = mutableListOf<Any>()
        var currentIndex = index

        when (realmQuery) {
            is RQuery.Where -> {
                filter.append("${realmQuery.field} ${realmQuery.operator.value} $${index}")
                arguments.add(realmQuery.value)
            }
            is RQuery.And -> {
                val subQueries = realmQuery.queries.map { subQuery ->
                    val (subFilter, subArgs) = buildQuery(subQuery, currentIndex)
                    currentIndex += subArgs.size
                    subFilter to subArgs
                }
                filter.append(subQueries.joinToString("AND", "(", ")") { "(${it.first})" })
                arguments.addAll(subQueries.flatMap { it.second })
            }
            is RQuery.Or -> {
                val subQueries = realmQuery.queries.map { subQuery ->
                    val (subFilter, subArgs) = buildQuery(subQuery, currentIndex)
                    currentIndex += subArgs.size
                    subFilter to subArgs
                }
                filter.append(subQueries.joinToString("OR", "(", ")") { "(${it.first})" })
                arguments.addAll(subQueries.flatMap { it.second })
            }
        }
        return Pair(filter.toString(), arguments)
    }
}
