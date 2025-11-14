package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
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

    override fun update(map: Map<String, Any>) {}

    override fun setUpAfterCreation(map: Map<String, Any>) {
        super.setUpAfterCreation(map)
        handleHospital(map)
        handleDoctor(map)
        handleMedicalSubSpecialty(map)
    }

    private fun handleHospital(map: Map<String, Any>) {
        if (!isValid()) return
        if (hospital != null) return
        val hospitalId = map["hospital_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            launch {
                FireStoreManager.observeDoc(FireStoreCollection.HOSPITALS, hospitalId)
            }
            launch {
                RealmManager.link(
                    hospitalId,
                    Hospital::class,
                    id,
                    DoctorAppointment::class,
                    DoctorAppointment::hospital
                )
            }
        }
    }

    private fun handleDoctor(map: Map<String, Any>) {
        if (!isValid()) return
        if (doctor != null) return
        val doctorId = map["doctor_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            launch {
                FireStoreManager.observeDoc(FireStoreCollection.DOCTORS, hospitalId)
            }
            launch {
                RealmManager.link(
                    doctorId,
                    Doctor::class,
                    id,
                    DoctorAppointment::class,
                    DoctorAppointment::doctor
                )
            }
        }
    }

    private fun handleMedicalSubSpecialty(map: Map<String, Any>) {
        if (!isValid()) return
        if (medicalSubSpecialty != null) return
        val medicalSubSpecialtyId = map["medical_sub_specialty_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            launch {
                FireStoreManager.observeDoc(FireStoreCollection.MEDICAL_SUB_SPECIALTIES, hospitalId)
            }
            launch {
                RealmManager.link(
                    medicalSubSpecialtyId,
                    MedicalSubSpecialty::class,
                    id,
                    DoctorAppointment::class,
                    DoctorAppointment::medicalSubSpecialty
                )
            }
        }
    }

    override fun removeDependencies() {
        super.removeDependencies()
        if (!isValid()) return

        FireStoreManager.removeListener(FireStoreCollection.HOSPITALS, hospitalId)
        FireStoreManager.removeListener(FireStoreCollection.MEDICAL_SUB_SPECIALTIES, medicalSubSpecialtyId)
        FireStoreManager.removeListener(FireStoreCollection.DOCTORS, doctorId)
    }

    override fun handleNestedObjects(
        map: Map<String, Any>,
        coroutine: CoroutineScope,
        configuration: RealmConfiguration?
    ) {}
}