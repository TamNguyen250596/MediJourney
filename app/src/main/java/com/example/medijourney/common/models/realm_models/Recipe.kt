package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmObject

class Recipe: RealmObject, RealmCycle {

    // Properties
    var id: Int = 0
    var tag: String = ""
    var position: Int = 0
    var enable: Boolean = false
    var name: String? = null
    var imageName: String? = null
    var nutritionDescription: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return Recipe().apply {
            id = map.getInt("id")
            tag = map["tag"] as? String ?: ""
            enable = map["enable"] as? Boolean == true
            position = map.getInt("position")
            name = map.getLocalizedString("name_localized")
            imageName = map["image_name"] as? String
            nutritionDescription = map.getLocalizedString("nutrition_description")
        }
    }

    override fun update(map: Map<String, Any>) {
        enable = map["enable"] as? Boolean ?: enable
        position = map["position"] as? Int ?: position
        name = map.getLocalizedString("name_localized", name)
        imageName = map["image_name"] as? String ?: imageName
        nutritionDescription = map.getLocalizedString("nutrition_description", nutritionDescription)
    }
}