package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class Hospital: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var tag: String = ""
    var imageName: String? = null
    var name: String? = null
    var location: String? = null
    var keywords: RealmSet<String> = realmSetOf()
    var medicalSubSpecialties: RealmSet<String> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return Hospital().apply {
            id = map["id"] as? String ?: ""
            tag = map["tag"] as? String ?: ""
            imageName = map["image_name"] as? String
            name = map["name"] as? String
            location = map["location"] as? String
            keywords.addAll(map.getStringSet("keywords"))
            medicalSubSpecialties.addAll(map.getStringSet("medical_sub_specialties"))
        }
    }

    override fun update(map: Map<String, Any>) {
        tag = map["tag"] as? String ?: tag
        imageName = map["image_name"] as? String
        name = map["name"] as? String
        location = map["location"] as? String
        keywords.addAll(map.getStringSet("keywords", keywords))
        medicalSubSpecialties.addAll(map.getStringSet("medical_sub_specialties", medicalSubSpecialties))
    }
}