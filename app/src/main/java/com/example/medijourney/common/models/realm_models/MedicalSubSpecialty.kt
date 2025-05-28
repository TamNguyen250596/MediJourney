package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class MedicalSubSpecialty: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var imageName: String? = null
    var medicalSpecialityId: String = ""
    var tag: String = ""
    var title: String? = null
    var actionUrl: String? = null
    var position: Int = 0
    var keywords: RealmSet<String> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return MedicalSubSpecialty().apply {
            id = map["id"] as? String ?: id
            imageName = map["image_name"] as? String
            medicalSpecialityId = map["medical_speciality_id"] as? String ?: medicalSpecialityId
            tag = map["tag"] as? String ?: ""
            title = map.getLocalizedString("title_localized")
            actionUrl = map["action_url"] as? String
            position = map.getInt("position")
            keywords.addAll(map.getStringSet("keywords"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        imageName = map["image_name"] as? String ?: imageName
        medicalSpecialityId = map["medical_speciality_id"] as? String ?: medicalSpecialityId
        tag = map["tag"] as? String ?: tag
        title = map.getLocalizedString("title_localized", title)
        actionUrl = map["action_url"] as? String ?: actionUrl
        position = map["position"] as? Int ?: position
        keywords.addAll(map.getStringSet("keywords", keywords))
    }
}