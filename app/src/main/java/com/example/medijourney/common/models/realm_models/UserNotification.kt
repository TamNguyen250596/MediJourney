package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserNotification: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var imageURL: String? = null
    var title: String? = null
    var description: String? = null
    var isRead: Boolean = false
    var createdAt: RealmInstant? = null
    var type: String? = null
    var relatedObjectType: String? = null
    var relatedObjectId: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserNotification().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            imageURL = map["image_url"] as? String
            title = map["title"] as? String
            description = map["description"] as? String
            isRead = map["is_read"] as? Boolean ?: isRead
            createdAt = map.getRealmInstant("created_at")
            type = map["type"] as? String
            relatedObjectType = map["related_object_type"] as? String
            relatedObjectId = map["related_object_id"] as? String
        }
    }

    override fun update(map: Map<String, Any>) {
        imageURL = map["image_url"] as? String ?: imageURL
        title = map["title"] as? String ?: title
        description = map["description"] as? String ?: description
        isRead = map["is_read"] as? Boolean ?: isRead
        type = map["type"] as? String ?: type
    }

    fun getIconName(): String? {
        if (!this.isValid()) return null

        return "ic_notification"
    }

    fun getNotificationType(): NotificationType? {
        if (!this.isValid()) return null
        val type = this.type ?: return null

        return try {
            NotificationType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

enum class NotificationType() {
    ADD_FITNESS_TRACKER_SUCCESS,
    ADD_FITNESS_TRACKER_FAILED,
    DAILY_SLEEP_TRACKING_REPORT,
    DAILY_NUTRITION_TRACKING_REPORT,
    DAILY_EXERCISE_TRACKING_REPORT,
    RECEIVE_NEW_MESSAGE
}