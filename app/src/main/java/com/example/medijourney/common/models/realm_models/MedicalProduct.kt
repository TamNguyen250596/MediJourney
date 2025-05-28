package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class MedicalProduct: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var imageName: String? = null
    var name: String? = null
    var description: String? = null
    var price: Double = 0.0
    var position: Int = 0
    var unit: String? = null
    var keywords: RealmSet<String> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return MedicalProduct().apply {
            id = map["id"] as? String ?: id
            imageName = map["image_name"] as? String
            name = map["name"] as? String
            description = map["description"] as? String
            price = map.getDouble("price")
            position = map.getInt("position")
            unit = map["unit"] as? String
            keywords.addAll(map.getStringSet("keywords"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        imageName = map["image_name"] as? String ?: imageName
        name = map["name"] as? String ?: name
        description = map["description"] as? String ?: description
        price = map.getDouble("price", price)
        unit = map["unit"] as? String ?: unit
        position = map.getInt("position", position)
    }
}