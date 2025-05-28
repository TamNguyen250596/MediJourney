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
    var userCode: String = ""
    var recommendReceipts: RealmSet<Int> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return UserRecommendRecipe().apply {
            id = map["id"] as? String ?: id
            userCode = map["user_code"] as? String ?: userCode
            recommendReceipts.addAll(map.getIntSet("recommend_receipts"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        recommendReceipts.addAll(map.getIntSet("recommend_exercises", recommendReceipts))
    }
}