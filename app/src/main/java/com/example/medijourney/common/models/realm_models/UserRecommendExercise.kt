package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getIntSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class UserRecommendExercise: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userId: String = ""
    var recommendExercises: RealmSet<Int> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserRecommendExercise().apply {
            id = map["id"] as? String ?: id
            userId = map["user_id"] as? String ?: userId
            recommendExercises.addAll(map.getIntSet("recommend_exercises"))
        }
    }

    override fun update(map: Map<String, Any>) {
        recommendExercises.addAll(map.getIntSet("recommend_exercises", recommendExercises))
    }
}