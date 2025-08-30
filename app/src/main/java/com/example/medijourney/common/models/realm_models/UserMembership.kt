package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserMembership: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var membershipId = 0
    var expiredAt: RealmInstant? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserMembership().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            membershipId = map.getInt("membership_id")
            expiredAt = map.getRealmInstant("expired_at")
        }
    }

    override fun update(map: Map<String, Any>) {
        membershipId = map.getInt("membership_id", membershipId)
        expiredAt = map.getRealmInstant("expired_at", expiredAt)
    }
}