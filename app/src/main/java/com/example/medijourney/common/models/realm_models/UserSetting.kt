package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserSetting: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var enableOTPAuth: Boolean = false
    var unreadNotificationCount: Int = 0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return UserSetting().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            enableOTPAuth = map["enable_otp_auth"] as? Boolean ?: enableOTPAuth
            unreadNotificationCount = map.getInt("unread_notification_count")
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        enableOTPAuth = map["enable_otp_auth"] as? Boolean ?: enableOTPAuth
        unreadNotificationCount = map.getInt("unread_notification_count", unreadNotificationCount)
    }
}
