package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class Message: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var message: String? = null
    var mediaName: String? = null
    var mediaUrl: String? = null
    var mediaType: String? = null
    var senderId: String = ""
    var senderName: String? = null
    var senderImageName: String? = null
    var createdAt: RealmInstant? = null
    var conversationId: String = ""
    var isPinned: Boolean = false
    var keywords: RealmSet<String> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return Message().apply {
            id = map["id"] as? String ?: ""
            message = map["message"] as? String
            mediaName = map["media_name"] as? String
            mediaUrl = map["media_url"] as? String
            mediaType = map["media_type"] as? String
            senderId = map["sender_id"] as? String ?: ""
            senderName = map["sender_name"] as? String
            senderImageName = map["sender_image_name"] as? String
            createdAt = map.getRealmInstant("created_at")
            conversationId = map["conversation_id"] as? String ?: ""
            isPinned = map["is_pinned"] as? Boolean ?: false
            keywords.addAll(map.getStringSet("keywords"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        message = map["message"] as? String ?: message
        mediaName = map["media_name"] as? String ?: mediaName
        mediaUrl = map["media_url"] as? String ?: mediaUrl
        mediaType = map["media_type"] as? String ?: mediaType
        senderId = map["sender_id"] as? String ?: senderId
        senderName = map["sender_name"] as? String ?: senderName
        senderImageName = map["sender_image_name"] as? String ?: senderImageName
        isPinned = map["is_pinned"] as? Boolean ?: isPinned
        keywords.addAll(map.getStringSet("keywords", keywords))
    }
}