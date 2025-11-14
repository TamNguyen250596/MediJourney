package com.example.medijourney.common.managers.realm

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.extensions.toFlow
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

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
            val realmObject = realmEntryInstance.create(data)
            realmEntryInstance.didInit(data)

            try {
                copyToRealm(realmObject, UpdatePolicy.ERROR)
            } catch (_: Exception) {
                realmEntryInstance.update(data)
            }
        }
    }

    suspend fun write(
        realmObject: RealmObject,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) {
        if (realmObject !is RealmCycle) return
        val realm = createRealm(configuration)

        realm.write {
            val primaryKey = data["id"]
            val kClass = realmObject.javaClass.kotlin
            val primaryKeyName = realmObject.primaryKey()
            val existingEntity = query(kClass, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
            if (existingEntity != null) {
                existingEntity.update(data)
            } else {
                val unmanagedEntity = realmObject.create(data)
                val entity = copyToRealm(unmanagedEntity, UpdatePolicy.ALL)
                if (entity is RealmCycle) {
                    entity.didInit(data)
                }
            }
        }
    }

    suspend fun write(
        realmObject: RealmObject,
        dataList: List<Map<String, Any>>,
        configuration: RealmConfiguration? = null
    ) {
        if (realmObject !is RealmCycle) return
        val realm = createRealm(configuration)

        realm.write {
            for (data in dataList) {
                val primaryKey = data["id"]
                val kClass = realmObject.javaClass.kotlin
                val primaryKeyName = realmObject.primaryKey()
                val existingEntity = query(kClass, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
                val scope = CoroutineScope(Dispatchers.IO)
                if (existingEntity != null) {
                    existingEntity.update(data)
                    existingEntity.handleNestedObjects(data, scope, configuration)
                } else {
                    val unmanagedEntity = realmObject.create(data)
                    val entity = copyToRealm(unmanagedEntity, UpdatePolicy.ALL)
                    if (entity is RealmCycle) {
                        entity.didInit(data)
                        entity.handleNestedObjects(data, scope, configuration)
                    }
                }
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
                val realmObject = realmEntryInstance.create(data)
                realmEntryInstance.didInit(data)

                try {
                    copyToRealm(realmObject, UpdatePolicy.ERROR)
                } catch (_: Exception) {
                    realmEntryInstance.update(data)
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
        clazz: Class<T>,
        realmQuery: RQuery? = null,
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

    fun <T : RealmObject> flow(
        kClazz: KClass<T>,
        id: String,
        configuration: RealmConfiguration? = null
    ): Flow<T?> {
        val realm = createRealm(configuration)
        val entities = realm.query(kClazz)
            .query("id == $0", id)

        return entities.toFlow().map { it.firstOrNull() }
    }

    fun <T : RealmObject> flow(
        kClazz: KClass<T>,
        queryBuilder: RQueryBuilder? = null,
        configuration: RealmConfiguration? = null
    ): Flow<List<T>> {
        val realm = createRealm(configuration)
        var entities = realm.query(kClazz)
        queryBuilder?.let {
            entities =  queryBuilder.applyTo(entities)
        }

        return entities.toFlow()
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
            entity?.update(data)
        }
    }

    suspend fun <T> update(
        kClass: KClass<T>,
        queryBuilder: RQueryBuilder? = null,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            var entities = query(kClass)
            queryBuilder?.let {
                entities = it.applyTo(entities)
            }
            for (entity in entities.find()) {
                entity.update(data)
                entity.handleNestedObjects(data, CoroutineScope(Dispatchers.IO), configuration)
            }
        }
    }

    suspend fun <T : RealmObject, R : RealmObject> linkEntity(
        id: String,
        entityClass: Class<T>,
        relatedClass: Class<R>,
        relationProperty: KMutableProperty1<T, R?>,
        configuration: RealmConfiguration? = null
    ) {
        val realm = createRealm(configuration)

        realm.write {
            val entity = query(entityClass.kotlin, "id == $0", id)
                .find()
                .firstOrNull { relationProperty.get(it) == null }

            val relatedEntity = query(relatedClass.kotlin, "id == $0", id)
                .find()
                .firstOrNull()

            if (entity != null && relatedEntity != null) {
                relationProperty.set(entity, relatedEntity)
            }
        }
    }

    suspend fun <T> update(
        entity: T,
        data: Map<String, Any>,
        configuration: RealmConfiguration? = null
    ) where T : RealmCycle, T : RealmObject {
        val realm = createRealm(configuration)

        realm.write {
            findLatest(entity)?.update(data)
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
                entity?.update(data)
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

    suspend fun delete(
        realmObject: RealmObject,
        primaryKey: String,
        configuration: RealmConfiguration? = null
    ) {
        if (realmObject !is RealmCycle) return
        val realm = createRealm(configuration)

        realm.write {
            val primaryKeyName = realmObject.primaryKey()
            val entity = query(realmObject.javaClass.kotlin, "$primaryKeyName == $0", primaryKey).find().firstOrNull()
            if (entity is RealmCycle) {
                entity.removeDependencies()
            }
            entity?.let {
                delete(it)
            }
        }
    }

    suspend fun delete(
        realmObject: RealmObject,
        primaryKeys: List<*>,
        configuration: RealmConfiguration? = null
    ) {
        if (realmObject !is RealmCycle) return
        val realm = createRealm(configuration)

        realm.write {
            val primaryKeyName = realmObject.primaryKey()
            val entities = query(realmObject.javaClass.kotlin, "$primaryKeyName IN $0", primaryKeys).find()
            for (entity in entities) {
                entity.removeDependencies()
            }
            delete(entities)
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

    // Query
    fun <T : RealmObject> query(
        clazz: Class<T>,
        configuration: RealmConfiguration? = null
    ): RealmQuery<T> {
        val realm = createRealm(configuration)
        return realm.query(clazz.kotlin)
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
