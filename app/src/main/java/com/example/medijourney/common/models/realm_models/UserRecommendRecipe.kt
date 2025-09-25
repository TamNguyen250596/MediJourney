package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getIntSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class UserRecommendRecipe: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userId: String = ""
    var recommendReceipts: RealmSet<Int> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserRecommendRecipe().apply {
            id = map["id"] as? String ?: id
            userId = map["user_id"] as? String ?: userId
            recommendReceipts.addAll(map.getIntSet("recommend_receipts"))
        }
    }

    override fun update(map: Map<String, Any>) {
        recommendReceipts.addAll(map.getIntSet("recommend_exercises", recommendReceipts))
    }
}