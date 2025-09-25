package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getIntList
import com.example.medijourney.common.extensions.getIntMap
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserExercisePlan:  RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userId: String = ""
    var createdAt: RealmInstant? = null
    var name: String? = null
    var duration: Double = 0.0
    var exercises: RealmList<Int> = realmListOf()
    var levels: RealmDictionary<Int> = realmDictionaryOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserExercisePlan().apply {
            id = map["id"] as? String ?: id
            userId = map["user_id"] as? String ?: userId
            createdAt = map.getRealmInstant("created_at")
            name = map["name"] as? String ?: name
            duration = map.getDouble("duration")
            exercises.addAll(map.getIntList("exercises"))
            levels.putAll(map.getIntMap("levels"))
        }
    }

    override fun update(map: Map<String, Any>) {
        createdAt = map.getRealmInstant("created_at", createdAt)
        name = map["name"] as? String ?: name
        duration = map.getDouble("duration", duration)
        exercises.clear()
        exercises.addAll(map.getIntList("exercises"))
        levels.clear()
        levels.putAll(map.getIntMap("levels"))
    }
}