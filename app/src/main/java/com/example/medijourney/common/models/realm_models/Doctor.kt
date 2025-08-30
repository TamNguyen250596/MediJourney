package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getStringSet
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class Doctor: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var doctorName: String? = null
    var doctorImage: String? = null
    var doctorSubSpecialities: RealmSet<String> = realmSetOf()
    var workingHospitals: RealmSet<String> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return Doctor().apply {
            id = map["id"] as? String ?: ""
            doctorName = map["doctor_name"] as? String
            doctorImage = map["doctor_image"] as? String
            doctorSubSpecialities.addAll(map.getStringSet("doctor_sub_specialities"))
            workingHospitals.addAll(map.getStringSet("working_hospitals"))
        }
    }

    override fun update(map: Map<String, Any>) {
        doctorImage = map["doctor_image"] as? String
        doctorSubSpecialities.addAll(map.getStringSet("doctor_sub_specialities"))
        workingHospitals.addAll(map.getStringSet("working_hospitals"))
    }
}