package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Advertisement:  RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var position: Int = 0
    var imageName: String? = null
    var actionUrl: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return Advertisement().apply {
            id = map["id"] as? String ?: ""
            position = map.getInt("position")
            imageName = map["image_name"] as? String
            actionUrl = map["action_url"] as? String
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        position = map.getInt("position", position)
        imageName = map["image_name"] as? String ?: imageName
        actionUrl = map["action_url"] as? String ?: actionUrl
    }
}