package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getIntList
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Exercise: RealmObject, RealmCycle {

    @PrimaryKey
    var id: Int = 0
    var tag: String = ""
    var position: Int = 0
    var enable: Boolean = false
    var name: String? = null
    var description: String? = null
    var imageName: String? = null
    var levels: RealmList<Int> = realmListOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return Exercise().apply {
            id = map.getInt("id")
            tag = map["tag"] as? String ?: ""
            enable = map["enable"] as? Boolean == true
            position = map.getInt("position")
            name = map.getLocalizedString("name_localized")
            description = map.getLocalizedString("description_localized")
            imageName = map["image_name"] as? String
            levels.addAll(map.getIntList("levels"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        enable = map["enable"] as? Boolean ?: enable
        position = map["position"] as? Int ?: position
        name = map.getLocalizedString("name_localized", name)
        description = map.getLocalizedString("description_localized", description)
        imageName = map["image_name"] as? String ?: imageName
        levels.addAll(map.getIntList("levels", levels))
    }
}