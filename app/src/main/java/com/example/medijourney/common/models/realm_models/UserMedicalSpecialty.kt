package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getStringList
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserMedicalSpecialty: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userId: String = ""
    var followingMedicalSpecialties: RealmList<String> = realmListOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserMedicalSpecialty().apply {
            id = map.getUserObjectKey(id)
            userId = map["user_id"] as? String ?: userId
            followingMedicalSpecialties.addAll(map.getStringList("following_medical_specialties"))
        }
    }

    override fun update(map: Map<String, Any>) {
        followingMedicalSpecialties.addAll(map.getStringList("following_medical_specialties", followingMedicalSpecialties))
    }
}