package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FitnessTrackerActivity: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var description: String? = null
    var tag: String = ""
    var enable: Boolean = false
    var position: Int = 0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return FitnessTrackerActivity().apply {
            id = map.getInt("id")
            name = map.getLocalizedString("name_localized")
            description = map.getLocalizedString("description_localized")
            tag = map["tag"] as? String ?: ""
            enable = map["enable"] as? Boolean == true
            position = map.getInt("position")
        }
    }

    override fun update(map: Map<String, Any>) {
        name = map.getLocalizedString("name_localized", name)
        description = map.getLocalizedString("description_localized", description)
        enable = map["enable"] as? Boolean ?: enable
        position = map["position"] as? Int ?: position
    }
}