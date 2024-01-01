package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class MedicalSpecialty: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var name: String? = null
    var enable: Boolean = false
    var position: Int = 0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return MedicalSpecialty().apply {
            id = map["id"] as? String ?: id
            name = map.getLocalizedString("name_localized")
            enable = map["enable"] as? Boolean ?: false
            position = map.getInt("position")
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        name = map.getLocalizedString("name_localized", name)
        enable = map["enable"] as? Boolean ?: enable
        position = map["position"] as? Int ?: position
    }
}
