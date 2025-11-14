package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getBooleanMap
import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class Conversation: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var tag: String = ""
    var imageName: String? = null
    var name: String = ""
    var description: String? = null
    var shortTag: String? = null
    var keywords: RealmSet<String> = realmSetOf()
    var lastMessage: String? = null
    var includeCurrentUser: Boolean = false

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return Conversation().apply {
            id = map["id"] as? String ?: ""
            tag = map["tag"] as? String ?: ""
            imageName = map["image_name"] as? String
            name = map["name"] as? String ?: ""
            description = map["description"] as? String
            shortTag = map["short_tag"] as? String
            lastMessage = map["last_message"] as? String
            keywords.addAll(map.getStringSet("keywords"))
            includeCurrentUser = isIncludeCurrentUser(map)
        }
    }

    override fun update(map: Map<String, Any>) {
        tag = map["tag"] as? String ?: tag
        imageName = map["image_name"] as? String ?: imageName
        name = map["name"] as? String ?: name
        description = map["description"] as? String ?: description
        shortTag = map["short_tag"] as? String ?: shortTag
        keywords.addAll(map.getStringSet("keywords", keywords))
        lastMessage = map["last_message"] as? String ?: lastMessage
        includeCurrentUser = isIncludeCurrentUser(map)
    }

    private fun isIncludeCurrentUser(map: Map<String, Any>): Boolean {
        if (map["include_current_user"] != null) {
            return map["include_current_user"] as? Boolean ?: false
        } else {
            val memberIds = map.getBooleanMap("member_ids")
            val currentUserId = FAManger.currentUserCode
            return memberIds[currentUserId] ?: false
        }
    }
}