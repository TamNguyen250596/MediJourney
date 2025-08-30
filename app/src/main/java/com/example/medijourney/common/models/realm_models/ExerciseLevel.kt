package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ExerciseLevel: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var enable: Boolean = false
    var level: String = ""
    var duration: Double = 0.0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return ExerciseLevel().apply {
            id = map.getInt("id")
            name = map.getLocalizedString("name")
            enable = map["enable"] as? Boolean == true
            level = map["level"] as? String ?: ""
            duration = map.getDouble("duration")
        }
    }

    override fun update(map: Map<String, Any>) {
        name = map.getLocalizedString("name", name)
        enable = map["enable"] as? Boolean ?: enable
        level = map["level"] as? String ?: level
        duration = map.getDouble("duration", duration)
    }
}