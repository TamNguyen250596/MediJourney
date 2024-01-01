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
    var userCode: String = ""
    var followingMedicalSpecialties: RealmList<String> = realmListOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return UserMedicalSpecialty().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            followingMedicalSpecialties.addAll(map.getStringList("following_medical_specialties"))
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        followingMedicalSpecialties.addAll(map.getStringList("following_medical_specialties", followingMedicalSpecialties))
    }
}