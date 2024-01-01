package com.example.medijourney.modules.dashboard.add_medical_booking

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.awaitGet
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.to

class AddMedicalBookingViewModel : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private val _enableAddMedicalBooking = MutableStateFlow(false)
    val enableAddMedicalBooking: StateFlow<Boolean> = _enableAddMedicalBooking.asStateFlow()
    private val _displayDatePicker = MutableStateFlow(false)
    val displayDatePicker: StateFlow<Boolean> = _displayDatePicker.asStateFlow()
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _displayDoctorTimePicker = MutableStateFlow(false)
    val displayDoctorTimePicker: StateFlow<Boolean> = _displayDoctorTimePicker.asStateFlow()
    private val _doctorTimeItemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val doctorTimeItemModels: StateFlow<List<DynamicUIItem>> = _doctorTimeItemModels.asStateFlow()
    private val _displayPaymentPicker = MutableStateFlow(false)
    val displayPaymentPicker: StateFlow<Boolean> = _displayPaymentPicker.asStateFlow()
    private val _paymentMethodItemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val paymentMethodItemModels: StateFlow<List<DynamicUIItem>> = _paymentMethodItemModels.asStateFlow()
    var initialSelectedDateMillis: Long? = null
    var selectedDoctorTimeItemTag: String? = null
    var selectedPaymentMethodItemTag: String? = null
    private var currentAddMedicalBooking: Map<String, Any> = emptyMap()
    private var doctorAppointmentResults: RealmResults<DoctorAppointment>? = null
    private var mapDataAtTag: MutableMap<String, Map<String, String>> = mutableMapOf()
    private var doctorAppointmentId: String? = null
    private var doctorAppointment: DoctorAppointment? = null

    // Life cycle
    fun onViewCreated(doctorAppointmentId: String?) {
        this.doctorAppointmentId = doctorAppointmentId
        currentAddMedicalBooking = InternationManager.getCurrentAppAddMedicalBooking()
        doctorAppointmentId?.let {
            viewModelScope.launch {
                getDoctorAppointment(it)
                _itemModels.value = generatePreviewItemModels(currentAddMedicalBooking)
            }
        } ?: run {
            _itemModels.value = generateEditableItemModels(currentAddMedicalBooking)
        }
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    private fun generateEditableItemModels(map: Map<String, Any>): List<DynamicUIItem> {
        val attributes = map["attributes"] as List<Map<String, Any>>
        return attributes.mapIndexed { index, attribute ->
            DynamicUIItem.fromMap(attribute).apply {
                additionalData = mapOf(Constants.ENABLE_FIELD to checkToShowField(index, itemModels.value))
            }
        }
    }

    private fun checkToShowField(index: Int, itemModels: List<DynamicUIItem>): Boolean {
        if (index == 0) return true
        if (itemModels.isEmpty()) return false
        return itemModels[index - 1].description?.text != null
    }

    private suspend fun getDoctorAppointment(id: String) {
        doctorAppointment = RealmManager.read(DoctorAppointment::class.java, id)
    }

    @Suppress("UNCHECKED_CAST")
    private fun generatePreviewItemModels(map: Map<String, Any>): List<DynamicUIItem> {
        val attributes = map["attributes"] as List<Map<String, Any>>

        return attributes.mapIndexedNotNull { index, attribute ->
            val itemTag = attribute["item_tag"] as String? ?: return@mapIndexedNotNull null
            val value = getAvailableValue(itemTag) ?: return@mapIndexedNotNull null

            DynamicUIItem.fromMap(attribute).apply {
                additionalData = mapOf(Constants.ENABLE_FIELD to false)
                description = MTextStyle(value)
            }
        }
    }

    private fun getAvailableValue(itemTag: String): String? {
        val doctorAppointment = doctorAppointment ?: return null
        if (!doctorAppointment.isValid()) return null

        when (itemTag) {
            "hospital" -> {
                val hospital = doctorAppointment.hospital ?: return null
                if (!hospital.isValid()) return null
                return hospital.name
            }
            "medical_specialty" -> {
                val medicalSubSpecialty = doctorAppointment.medicalSubSpecialty ?: return null
                if (!medicalSubSpecialty.isValid()) return null
                return medicalSubSpecialty.title
            }
            "date" -> {
                return doctorAppointment.appointmentDateString
            }
            "doctor_and_time" -> {
                val doctor = doctorAppointment.doctor ?: return null
                if (!doctor.isValid()) return null
                return "${doctor.doctorName} (${doctorAppointment.startTime} - ${doctorAppointment.endTime})"
            }
            else -> null
        }
        return null
    }

    fun updateField(fieldTag: String, map: Map<String, String>)  {
        val value = map[Constants.VALUE] ?: return
        val id = map[Constants.OBJECT_ID]
        val currentList: MutableList<DynamicUIItem> = mutableListOf()
        var startResetIndex: Int? = null
        var enableAddMedicalBooking = false

        for (index in _itemModels.value.indices) {
            val item = _itemModels.value[index].copy()

            if (item.itemTag == fieldTag) {
                val itemDes = item.description?.text
                if (itemDes != value) {
                    if (itemDes != null) {
                        startResetIndex = index
                    }
                    item.description = MTextStyle(value)
                    if (id != null) {
                        item.data = id
                    }
                }
            } else {
                if (startResetIndex != null) {
                    item.description = null
                    item.data = null
                    item.additionalData = mapOf(Constants.ENABLE_FIELD to false)
                } else {
                    item.additionalData = mapOf(Constants.ENABLE_FIELD to checkToShowField(index, currentList))
                }
            }
            enableAddMedicalBooking = item.description != null
            currentList.add(item)
        }

        mapDataAtTag[fieldTag] = map
        _itemModels.value = currentList
        _enableAddMedicalBooking.value = enableAddMedicalBooking
    }

    // Get Object ID
    fun getObjectId(tag: String): String? {
        val mapData = mapDataAtTag[tag] ?: return null
        return mapData[Constants.OBJECT_ID]
    }

    // Date Field
    fun updateDisplayDatePicker(display: Boolean) {
        if (display) {
            _isLoading.value = true
            viewModelScope.launch {
                getDoctorAppointmentResultsFS()
                getDoctorAppointmentResults()
                _isLoading.value = false
                _displayDatePicker.value = true
            }
        } else {
            _displayDatePicker.value = false
        }
    }

    private suspend fun getDoctorAppointmentResultsFS() {
        val hospitalId = getObjectId("hospital") ?: return
        val subSpecialtyId = getObjectId("medical_specialty") ?: return

        try {
            val snapshot = FireStoreManager.buildCollectionRef(FireStoreCollection.DOCTORS_APPOINTMENTS)
                .whereEqualTo("patient_id", "")
                .whereEqualTo("hospital_id", hospitalId)
                .whereEqualTo("medical_sub_specialty_id", subSpecialtyId)
                .awaitGet()

            snapshot.documents.forEach {
                val data = it.data ?: return@forEach
                RealmManager.create(DoctorAppointment::class.java, data)
            }
        } catch (_: Exception) {
            _isLoading.value = false
        }
    }

    private suspend fun getDoctorAppointmentResults() {
        val hospitalId = getObjectId("hospital") ?: return
        val subSpecialtyId = getObjectId("medical_specialty") ?: return

        doctorAppointmentResults = RealmManager.read(
            clazz = DoctorAppointment::class.java,
            realmQuery = RQuery.And(
                listOf(
                    RQuery.Where(DoctorAppointment::patientId.name, Operator.EQUAL, ""),
                    RQuery.Where(DoctorAppointment::hospitalId.name, Operator.EQUAL, hospitalId),
                    RQuery.Where(DoctorAppointment::medicalSubSpecialtyId.name, Operator.EQUAL, subSpecialtyId)
                )
            ),
            sort = listOf(DoctorAppointment::appointmentDate.name to Sort.ASCENDING)
        )
    }

    fun checkSelectableDate(utcTimeMillis: Long): Boolean {
        val doctorAppointmentResults = doctorAppointmentResults ?: return true
        val date = DateHelper.convertUtcTimeMillisToLocalDate(utcTimeMillis) ?: return true
        doctorAppointmentResults.forEach {
            if (!it.isValid()) return@forEach
            val appointmentDate = it.appointmentDate ?: return@forEach
            val appointment = DateHelper.convertRealmInstantToLocalDate(appointmentDate) ?: return@forEach

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                date.month.value == appointment.month.value && date.dayOfMonth == appointment.dayOfMonth
            } else {
                true
            }
        }
        return true
    }

    fun checkSelectedYear(year: Int): Boolean {
        val doctorAppointmentResults = doctorAppointmentResults ?: return true

        val firstAppointment = doctorAppointmentResults.firstOrNull() ?: return true
        if (!firstAppointment.isValid()) return true
        val appointmentDate = firstAppointment.appointmentDate ?: return true
        val firstDate = DateHelper.convertRealmInstantToDate(appointmentDate)
        val minYear = DateHelper.getYear(firstDate)

        val lastAppointment = doctorAppointmentResults.lastOrNull() ?: return true
        if (!lastAppointment.isValid()) return true
        val appointmentDate2 = lastAppointment.appointmentDate ?: return true
        val lastDate = DateHelper.convertRealmInstantToDate(appointmentDate2)
        val maxYear = DateHelper.getYear(lastDate)

        return year >= minYear && year <= maxYear
    }

    fun updateDateField(dateMillis: Long?) {
        initialSelectedDateMillis = dateMillis
        if (dateMillis == null) return
        val localDate = DateHelper.convertUtcTimeMillisToLocalDate(dateMillis) ?: return
        val value = DateHelper.convertLocalDateToString(localDate, "dd/MM/yyyy")
        val mapData = mapOf(Constants.VALUE to value)
        updateField("date", mapData)
    }

    // Doctor and Time Field
    fun updateDoctorTimePicker(display: Boolean) {
        if (display) {
            val mapData = mapDataAtTag["date"] ?: return
            val dateString = mapData[Constants.VALUE] ?: return
            val doctorAppointments = getDoctorAppointments(dateString) ?: return
            _doctorTimeItemModels.value = generateDoctorTimeItemModels(doctorAppointments)
        }
        _displayDoctorTimePicker.value = display
    }

    private fun getDoctorAppointments(dateString: String): List<DoctorAppointment>? {
        val doctorAppointmentResults = doctorAppointmentResults ?: return null
        return doctorAppointmentResults.filter {
           val appointmentDateString = it.appointmentDateString ?: return@filter false
            appointmentDateString == dateString
        }
    }

    private fun generateDoctorTimeItemModels(list: List<DoctorAppointment>): List<DynamicUIItem> {
        if (list.isEmpty()) return emptyList()

        return list.mapNotNull {
            if (!it.isValid()) return@mapNotNull null
            val startTime = it.startTime ?: return@mapNotNull null
            val endTime = it.endTime ?: return@mapNotNull null
            val doctor = it.doctor ?: return@mapNotNull null
            val doctorName = doctor.doctorName ?: return@mapNotNull null
            if (!doctor.isValid()) return@mapNotNull null
            val value = "$startTime - $endTime"

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
                title = MTextStyle(doctorName),
                description = MTextStyle(value)
            )
        }
    }

    fun updateDoctorTimeField(itemModel: DynamicUIItem?) {
        val itemModel = itemModel ?: return
        selectedDoctorTimeItemTag = itemModel.itemTag

        val title = itemModel.title ?: return
        val description = itemModel.description ?: return
        val value = "${title.text} (${description.text})"
        val mapData = mapOf(Constants.VALUE to value, Constants.OBJECT_ID to itemModel.itemTag)
        updateField("doctor_and_time", mapData)
    }

    // Payment Method
    fun updatePaymentMethodPicker(display: Boolean) {
        if (display) {
            if (_paymentMethodItemModels.value.isEmpty()) {
                _paymentMethodItemModels.value = generatePaymentMethodItemModels()
            }
        }
        _displayPaymentPicker.value = display
    }

    @Suppress("UNCHECKED_CAST")
    private fun generatePaymentMethodItemModels(): List<DynamicUIItem> {
        val map = InternationManager.getCurrentAppPaymentMethods()
        val attributes = map["attributes"] as List<Map<String, Any>>
        return attributes.mapIndexed { index, attribute ->
            DynamicUIItem.fromMap(attribute)
        }
    }

    fun updatePaymentField(itemModel: DynamicUIItem?) {
        val itemModel = itemModel ?: return
        selectedPaymentMethodItemTag = itemModel.itemTag

        val title = itemModel.title ?: return
        val mapData = mapOf(Constants.VALUE to title.text)
        updateField("payment", mapData)
    }

    // Add Medical Booking
    fun addMedicalBooking(completion: (Boolean) -> Unit) {
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return completion.invoke(false)
        val doctorAndTimeItem = _itemModels.value.first { it.itemTag == "doctor_and_time" }
        val id = doctorAndTimeItem.data as String? ?: return completion.invoke(false)
        _isLoading.value = true

        FireStoreManager.buildDocRef(FireStoreCollection.DOCTORS_APPOINTMENTS to id)
            .update("patient_id", userCode)
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.update(DoctorAppointment::class.java, id, mapOf("patient_id" to userCode))
                    completion.invoke(true)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                completion.invoke(false)
                _isLoading.value = false
            }
    }

    // Delete Medical Booking
    fun deleteDoctorAppointment(completion: (Boolean) -> Unit) {
        _isLoading.value = true
        val id = doctorAppointmentId ?: return

        FireStoreManager.buildDocRef(FireStoreCollection.DOCTORS_APPOINTMENTS to id)
            .update("patient_id", "")
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.update(DoctorAppointment::class.java, id, mapOf("patient_id" to ""))
                    completion.invoke(true)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                completion.invoke(false)
                _isLoading.value = false
            }
    }
}