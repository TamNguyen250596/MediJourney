package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Membership: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var imageName: String? = null
    var position: Int = 0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return Membership().apply {
            id = map.getInt("id")
            name = map.getLocalizedString("name_localized")
            imageName = map["image_name"] as? String
            position = map.getInt("position")
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        name = map.getLocalizedString("name_localized", name)
        imageName = map["image_name"] as? String ?: imageName
        position = map.getInt("position", position)
    }
}