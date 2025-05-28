package com.example.medijourney.modules.profile.manage_onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.where
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.BaseItemModel
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.item_models.NestedRecycleItemModel
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.models.realm_models.MedicalSpecialty
import com.example.medijourney.common.models.realm_models.MedicalSubSpecialty
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.models.WebModel
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class ManageOnboardingViewModel: ViewModel() {

    // Properties
    var dataList = MutableLiveData<MutableList<BaseItemInterface>>(mutableListOf())
    var currentUpdatedIndex: Int? = null
    private var medicalSpecialtyResult: RealmResults<MedicalSpecialty>? = null
    private var subMedicalSpecialtyResult: RealmResults<MedicalSubSpecialty>? = null
    private var userMedicalSpecialtyResult: RealmResults<UserMedicalSpecialty>? = null
    private var currentLanguageCode = "vi"

    // Life cycle
    fun inputData(languageCode: String) {
        currentLanguageCode = languageCode
    }

    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            observeRealms()
        }
    }

    // Get data
    private suspend fun getData() {
        getMedicalSpecialty()
        getSubMedicalSpecialty()
        getUserMedicalSpecialty()
        dataList.postValue(generateCellModel())
    }

    private suspend fun getMedicalSpecialty() {
        medicalSpecialtyResult = RealmManager.read(
            MedicalSpecialty::class.java, sort =
            listOf(Pair(MedicalSpecialty::position.name, Sort.ASCENDING))
        )
    }

    private suspend fun getSubMedicalSpecialty() {
        subMedicalSpecialtyResult = RealmManager.read(
            MedicalSubSpecialty::class.java, sort =
            listOf(Pair(MedicalSubSpecialty::position.name, Sort.ASCENDING))
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
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun observeRealms() {
        val medicalSpecialtyResult = medicalSpecialtyResult ?: return
        val subMedicalSpecialtyResult = subMedicalSpecialtyResult ?: return
        val userMedicalSpecialtyResult = userMedicalSpecialtyResult ?: return

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
                val modelList = generateCellModel()
                dataList.postValue(modelList)
            }
    }

    // Functions
    private fun generateCellModel(): MutableList<BaseItemInterface> {
        val medicalSpecialtyResult = medicalSpecialtyResult
        val subMedicalSpecialtyResult = subMedicalSpecialtyResult
        val userMedicalSpecialty = userMedicalSpecialtyResult?.firstOrNull()
        if (medicalSpecialtyResult == null || subMedicalSpecialtyResult == null || userMedicalSpecialty == null) return mutableListOf()
        if (!userMedicalSpecialty.isValid()) return mutableListOf()
        val dataList: MutableList<BaseItemInterface> = mutableListOf()

        medicalSpecialtyResult.forEachIndexed { sectionIndex, specialty ->
            if (specialty.isValid() && specialty.enable) {
                val selectionItemModel = SelectionItemModel(
                    type = Constants.HEADER,
                    groupIndex = sectionIndex,
                    data = specialty,
                    title = MTextStyle(specialty.name ?: "").apply {
                        font = R.font.proximanova_bold
                    },
                    isSelected = userMedicalSpecialty.followingMedicalSpecialties.contains(specialty.id),
                )
                val nestedRecycleItemModel = NestedRecycleItemModel(
                    type = Constants.NESTED_RECYCLE_VIEW,
                    groupIndex = sectionIndex,
                    padding = EdgePadding(48, 48, 48, 48)
                )

                subMedicalSpecialtyResult
                    .filter { it.isValid() && it.medicalSpecialityId == specialty.id }
                    .forEachIndexed { index, medicalSubSpecialty ->
                        if (medicalSubSpecialty.isValid()) {
                            val imageItemModel = ImageItemModel(
                                type = Constants.ITEM,
                                itemTag = medicalSubSpecialty.tag,
                                groupIndex = sectionIndex,
                                data = medicalSubSpecialty,
                                image = ImageStyle().apply {
                                    url = "images/medical_subspecialties/${medicalSubSpecialty.imageName}.jpg"
                                },
                                title = MTextStyle(medicalSubSpecialty.title ?: "").apply {
                                    size = 10f
                                    font = R.font.proximanova_regular
                                },
                                padding = EdgePadding().apply {
                                    left = if (index == 0) 0 else 24
                                }
                            )
                            nestedRecycleItemModel.items.add(imageItemModel)
                        }
                    }
                if (nestedRecycleItemModel.items.isNotEmpty()) {
                    dataList.add(selectionItemModel)
                    dataList.add(nestedRecycleItemModel)
                    dataList.add(BaseItemModel())
                }
            }
        }

        return dataList
    }

    fun updateUserMedicalSpecialty(
        isChecked: Boolean,
        viewModel: BaseItemInterface,
        completion: ((Boolean) -> Unit)? = null
    ) {
        val medicalSpecialty = viewModel.data as? MedicalSpecialty ?: run {
            completion?.invoke(false)
            return
        }

        if (!medicalSpecialty.isValid()) {
            completion?.invoke(false)
            return
        }

        val userMedicalSpecialty = userMedicalSpecialtyResult?.firstOrNull() ?: run {
            completion?.invoke(false)
            return
        }

        if (!userMedicalSpecialty.isValid()) {
            completion?.invoke(false)
            return
        }

        val tempFollowingList = userMedicalSpecialty.followingMedicalSpecialties.toMutableList().apply {
            if (isChecked && !contains(medicalSpecialty.id)) {
                add(medicalSpecialty.id)
            } else {
                removeIf { it == medicalSpecialty.id }
            }
        }

        currentUpdatedIndex = dataList.value?.indexOf(viewModel)
        FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_MEDICAL_SPECIALTIES, userMedicalSpecialty.id))
            .update(mapOf("following_medical_specialties" to tempFollowingList))
            .addOnCompleteListener {
                completion?.invoke(it.isSuccessful)
            }
    }

    fun getWebViewModel(model: BaseItemInterface?): WebModel? {
        val realmObject = model?.data as? MedicalSubSpecialty ?: return null
        if (!realmObject.isValid()) return null
        val actionUrl = realmObject.actionUrl ?: return null
        return WebModel().apply {
            title = realmObject.title
            url = actionUrl
        }
    }
}