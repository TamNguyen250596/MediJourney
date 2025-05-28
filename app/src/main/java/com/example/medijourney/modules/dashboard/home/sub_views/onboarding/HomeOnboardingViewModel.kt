package com.example.medijourney.modules.dashboard.home.sub_views.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.where
import com.example.medijourney.common.models.WebModel
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.MedicalSpecialty
import com.example.medijourney.common.models.realm_models.MedicalSubSpecialty
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class HomeOnboardingViewModel: ViewModel() {

    // Properties
    private val _items = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val items: StateFlow<List<ImageItemModel>> = _items.asStateFlow()
    private var medicalSpecialtyResult: RealmResults<MedicalSpecialty>? = null
    private var subMedicalSpecialtyResult: RealmResults<MedicalSubSpecialty>? = null
    private var userMedicalSpecialtyResult: RealmResults<UserMedicalSpecialty>? = null

    // Life cycle
   fun onViewCreated() {
        viewModelScope.launch {
            getData()
            _items.value = generateCellModel()
            observeRealms()
            delay(1000)

        }
    }

    // Functions
    private suspend fun getData() {
        getMedicalSpecialty()
        getSubMedicalSpecialty()
        getUserMedicalSpecialty()
    }

    private suspend fun getMedicalSpecialty() {
        medicalSpecialtyResult = RealmManager.read(
            MedicalSpecialty::class.java,
            sort = listOf(Pair(MedicalSpecialty::position.name, Sort.ASCENDING))
        )
    }

    private suspend fun getSubMedicalSpecialty() {
        subMedicalSpecialtyResult = RealmManager.read(
            MedicalSubSpecialty::class.java,
            sort = listOf(Pair(MedicalSubSpecialty::position.name, Sort.ASCENDING))
        )
    }

    private suspend fun getUserMedicalSpecialty() {
        FirebaseAuthManager.getCurrentUserCode()?.let {
            userMedicalSpecialtyResult = RealmManager.read(
                UserMedicalSpecialty::class.java,
                realmQuery = where(UserMedicalSpecialty::userCode.name, Operator.EQUAL, it)
            )
        }
    }

    // Observe data
    @OptIn(FlowPreview::class)
    private suspend fun observeRealms() {
        val medicalSpecialtyResult = medicalSpecialtyResult
        val subMedicalSpecialtyResult = subMedicalSpecialtyResult
        val userMedicalSpecialtyResult = userMedicalSpecialtyResult
        if (medicalSpecialtyResult == null || subMedicalSpecialtyResult == null || userMedicalSpecialtyResult == null) return

        combine(
            medicalSpecialtyResult.asFlow(),
            subMedicalSpecialtyResult.asFlow(),
            userMedicalSpecialtyResult.asFlow()
        ) { medicalChanges, subMedicalChanges, userChanges ->
            this.medicalSpecialtyResult = medicalChanges.list
            this.subMedicalSpecialtyResult = subMedicalChanges.list
            this.userMedicalSpecialtyResult = userChanges.list
        }
            .debounce(5000)
            .collectLatest {
                _items.value = generateCellModel()
            }
    }

    // Functions
    private fun generateCellModel(): List<ImageItemModel> {
        val medicalSpecialtyResult = medicalSpecialtyResult
        val subMedicalSpecialtyResult = subMedicalSpecialtyResult
        val userMedicalSpecialty = userMedicalSpecialtyResult?.firstOrNull()
        if (medicalSpecialtyResult == null || subMedicalSpecialtyResult == null || userMedicalSpecialty == null) return listOf()
        if (!userMedicalSpecialty.isValid()) return listOf()

        return medicalSpecialtyResult
            .filter { specialty ->
                specialty.isValid() && specialty.enable &&
                        userMedicalSpecialty.isValid() &&
                        userMedicalSpecialty.followingMedicalSpecialties.contains(specialty.id)
            }
            .flatMap { specialty ->
                subMedicalSpecialtyResult
                    .filter { subSpecialty ->
                        subSpecialty.isValid() && subSpecialty.medicalSpecialityId == specialty.id
                    }
                    .take(5)
                    .map { subSpecialty ->
                        ImageItemModel(
                            itemTag = subSpecialty.tag,
                            data = subSpecialty,
                            image = ImageStyle().apply {
                                url = "images/medical_subspecialties/${subSpecialty.imageName}.jpg"
                            },
                            title = MTextStyle(subSpecialty.title ?: "").apply {
                                size = 10f
                                font = R.font.proximanova_regular
                            }
                        )
                    }
            }
    }

    fun getWebViewModel(model: ImageItemModel?): WebModel? {
        val realmObject = model?.data as? MedicalSubSpecialty ?: return null
        if (!realmObject.isValid()) return null
        val actionUrl = realmObject.actionUrl ?: return null
        return WebModel().apply {
            title = realmObject.title
            url = actionUrl
        }
    }
}