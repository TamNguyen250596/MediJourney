package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoctorAppointment: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var hospitalId: String = ""
    var hospital: Hospital? = null
    var medicalSubSpecialtyId: String = ""
    var medicalSubSpecialty: MedicalSubSpecialty? = null
    var doctorId: String = ""
    var doctor: Doctor? = null
    var patientId: String = ""
    var appointmentDateString: String? = null
    var appointmentDate: RealmInstant? = null
    var startTime: String? = null
    var endTime: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return DoctorAppointment().apply {
            id = map["id"] as? String ?: ""
            hospitalId = map["hospital_id"] as? String ?: ""
            medicalSubSpecialtyId = map["medical_sub_specialty_id"] as? String ?: ""
            doctorId = map["doctor_id"] as? String ?: ""
            patientId = map["patient_id"] as? String ?: ""
            appointmentDateString = map["appointment_date_string"] as? String
            appointmentDate = map.getRealmInstant("appointment_date")
            startTime = map["start_time"] as? String
            endTime = map["end_time"] as? String
        }
    }

    override fun update(map: Map<String, Any>) {
        patientId = map["patient_id"] as? String ?: patientId
        appointmentDateString = map["appointment_date_string"] as? String ?: appointmentDateString
        appointmentDate = map.getRealmInstant("appointment_date", appointmentDate)
        startTime = map["start_time"] as? String ?: startTime
        endTime = map["end_time"] as? String ?: endTime
    }

    override fun didInit(map: Map<String, Any>) {
        super.didInit(map)
        handleToSaveHospital(map)
        handleToSaveDoctor(map)
    }

    private fun handleToSaveHospital(map: Map<String, Any>) {
        val hospitalId = map["hospital_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            RealmManager.createRealm().write {
                val hospital = query(Hospital::class, "${Hospital::id.name} == $0", hospitalId).find().firstOrNull()
                query(
                    DoctorAppointment::class,
                    "${DoctorAppointment::hospitalId.name} == $0 AND ${DoctorAppointment::hospital.name} == $1", hospitalId, null)
                    .find()
                    .forEach { it.hospital = hospital }

            }
        }

        FireStoreManager.buildDoc(FireStoreCollection.HOSPITALS to hospitalId)
            .addListener {
                val data = it.data
                if (data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RealmManager.createRealm().write {
                            val existingHospital = query(Hospital::class, "${Hospital::id.name} == $0", hospitalId).find().firstOrNull()
                            if (existingHospital == null) {
                                val hospital = Hospital().create(data) as? Hospital ?: return@write
                                query(
                                    DoctorAppointment::class,
                                    "${DoctorAppointment::hospitalId.name} == $0 AND ${DoctorAppointment::hospital.name} == $1", hospitalId, null)
                                    .find()
                                    .forEach { it.hospital = copyToRealm(hospital) }
                            } else {
                                existingHospital.update(data)
                            }
                        }
                    }
                }
            }
    }

    private fun handleToSaveDoctor(map: Map<String, Any>) {
        val doctorId = map["doctor_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            RealmManager.createRealm().write {
                val doctor = query(Doctor::class, "${Doctor::id.name} == $0", doctorId).find().firstOrNull()
                query(
                    DoctorAppointment::class,
                    "${DoctorAppointment::doctorId.name} == $0 AND ${DoctorAppointment::doctor.name} == $1", doctorId, null)
                    .find()
                    .forEach { it.doctor = doctor }
            }
        }

        FireStoreManager.buildDoc(FireStoreCollection.DOCTORS to doctorId)
            .addListener {
                val data = it.data
                if (data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RealmManager.createRealm().write {
                            val existingDoctor = query(Doctor::class, "${Doctor::id.name} == $0", doctorId).find().firstOrNull()
                            if (existingDoctor == null) {
                                val doctor = Doctor().create(data) as? Doctor ?: return@write
                                query(
                                    DoctorAppointment::class,
                                    "${DoctorAppointment::doctorId.name} == $0 AND ${DoctorAppointment::doctor.name} == $1", doctorId, null)
                                    .find()
                                    .forEach { it.doctor = copyToRealm(doctor) }
                            } else {
                                existingDoctor.update(data)
                            }
                        }
                    }
                }
            }
    }

    override fun removeDependencies() {
        super.removeDependencies()
        if (!isValid()) return

        FireStoreManager.buildDoc(FireStoreCollection.HOSPITALS to hospitalId).remove()
        FireStoreManager.buildDoc(FireStoreCollection.MEDICAL_SUB_SPECIALTIES to medicalSubSpecialtyId).remove()
        FireStoreManager.buildDoc(FireStoreCollection.DOCTORS to doctorId).remove()
    }

    override fun handleNestedObjects(
        map: Map<String, Any>,
        coroutine: CoroutineScope,
        configuration: RealmConfiguration?
    ) {
        val medicalSubSpecialtyId = map["medical_sub_specialty_id"] as? String

        coroutine.launch {
            if (!medicalSubSpecialtyId.isNullOrBlank()) {
                RealmManager.linkEntity(
                    medicalSubSpecialtyId,
                    DoctorAppointment::class.java,
                    MedicalSubSpecialty::class.java,
                    DoctorAppointment::medicalSubSpecialty
                )
                FireStoreManager.observeDoc(
                    FireStoreCollection.MEDICAL_SUB_SPECIALTIES,
                    medicalSubSpecialtyId
                )
            }
        }
    }
}