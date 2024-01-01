package com.example.medijourney.modules.dashboard.home.sub_views.home_doctor_appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.DoctorAppointment
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.collections.mapNotNull

class HomeDoctorAppointmentViewModel: ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var appointmentResults: RealmResults<DoctorAppointment>? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            _itemModels.value = generateItemModels(appointmentResults)
            observeData()
        }
    }

    // Functions
    private suspend fun getData() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        appointmentResults = RealmManager.read(
            clazz = DoctorAppointment::class.java,
            realmQuery = RQuery.Where(DoctorAppointment::patientId.name, Operator.EQUAL, currentUserCode),
            sort = listOf(Pair(DoctorAppointment::appointmentDate.name, Sort.ASCENDING))
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val appointments = appointmentResults ?: return

        appointments
            .asFlow()
            .debounce(500)
            .collect {
                appointmentResults = it.list
                _itemModels.value = generateItemModels(appointmentResults)
            }
    }

    private fun generateItemModels(appointments: RealmResults<DoctorAppointment>?): List<DynamicUIItem> {
        appointments ?: return emptyList()

        return appointments
            .take(3)
            .mapNotNull {
            if (!it.isValid()) return@mapNotNull null
            val hospital = it.hospital
            val doctor = it.doctor
            val dateString = it.appointmentDateString
            val startTime = it.startTime
            val endTime = it.endTime

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
            ).apply {
                if (hospital != null && hospital.isValid()) {
                    hospital.name?.let {
                        title = MTextStyle(it)
                    }
                }
                if (doctor != null && doctor.isValid()) {
                    doctor.doctorName?.let {
                        description = MTextStyle(it)
                    }
                }
                if (dateString != null && startTime != null && endTime != null) {
                    secondaryDescription = MTextStyle("$dateString: $startTime - $endTime")
                }
            }
        }
    }

    fun getDoctorAppointmentId(item: DynamicUIItem): String? {
        val appointment = item.data as? DoctorAppointment ?: return null
        if (!appointment.isValid()) return null

        return appointment.id
    }
}