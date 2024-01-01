package com.example.medijourney.modules.dashboard.purchased_medical_product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserMedicalProduct
import com.example.medijourney.common.models.realm_models.UserMedicalProductStatus
import com.example.medijourney.common.models.ui_models.ImageStyle
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

class PurchasedMedicalProductListViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var userMedicalProductResults: RealmResults<UserMedicalProduct>? = null
    private var status: String = ""

    // Life cycle
    fun onViewCreated(medicalProductStatus: String) {
        status = medicalProductStatus
        viewModelScope.launch {
            getData(medicalProductStatus)
            _itemModels.value = generateItemModelList(userMedicalProductResults)
            observeData()
        }
    }

    // Functions
    private suspend fun getData(status: String) {
        userMedicalProductResults = RealmManager.read(
            clazz = UserMedicalProduct::class.java,
            realmQuery = RQuery.Where(UserMedicalProduct::status.name, Operator.EQUAL, status),
            sort = listOf(UserMedicalProduct::createdAt.name to Sort.DESCENDING)
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val userMedicalProductResults = userMedicalProductResults ?: return

        userMedicalProductResults
            .asFlow()
            .debounce(500)
            .collect {
                this.userMedicalProductResults = it.list
                _itemModels.value = generateItemModelList(userMedicalProductResults)
            }
    }

    private fun generateItemModelList(userMedicalProductResults: RealmResults<UserMedicalProduct>?): List<DynamicUIItem> {
        if (userMedicalProductResults.isNullOrEmpty()) return emptyList()

        return userMedicalProductResults.mapNotNull {
            if (!it.isValid()) return@mapNotNull null
            val medicalProduct = it.medicalProduct ?: return@mapNotNull null
            if (!medicalProduct.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0
            ).apply {
                medicalProduct.imageName?.let {
                    image = ImageStyle(url = "images/medical_products/${it}.png")
                }
                medicalProduct.name?.let { name ->
                    title = MTextStyle(name)
                }
                it.totalPriceString?.let { price ->
                    val numberOfItems = it.numberOfItems
                    val unit = medicalProduct.unit
                    description = MTextStyle(text = "$price / $numberOfItems $unit")
                }
                it.createdAt?.let { createdAt ->
                    val dateString = DateHelper.convertRealmInstantToString(createdAt, "dd/MM/yyyy")
                    secondaryDescription = MTextStyle(dateString)
                }
            }
        }
    }

    fun checkShowRate(itemModel: DynamicUIItem): Boolean {
        val data = itemModel.data as? UserMedicalProduct ?: return false
        if (!data.isValid()) return false

        return data.status == UserMedicalProductStatus.TO_RATE.name.lowercase()
    }

    fun checkEnableRate(itemModel: DynamicUIItem): Boolean {
        val data = itemModel.data as? UserMedicalProduct ?: return false
        if (!data.isValid()) return false

        return data.status == UserMedicalProductStatus.TO_RATE.name.lowercase() && !data.isRated
    }

    fun getInitialRate(itemModel: DynamicUIItem): Int {
        val data = itemModel.data as? UserMedicalProduct ?: return 0
        if (!data.isValid()) return 0

        return data.rate
    }

    fun checkShowConfirmButton(itemModel: DynamicUIItem): Boolean {
        val data = itemModel.data as? UserMedicalProduct ?: return false
        if (!data.isValid()) return false

        return data.status == UserMedicalProductStatus.TO_RECEIVE.name.lowercase()
    }

    fun seenProduct(indices: List<Int>) {
        val itemModels = _itemModels.value.toMutableList()

        indices.forEach { index ->
            val itemModel = itemModels.getOrNull(index) ?: return@forEach
            val data = itemModel.data as? UserMedicalProduct ?: return@forEach
            if (!data.isValid()) return@forEach

            val map = mapOf(
                "is_read" to true
            )

            FireStoreManager.buildUserDocRef(FireStoreCollection.USER_MEDICAL_PRODUCTS to data.id)
                .update(map)
        }
    }

    fun confirmToReceiveProduct(itemModel: DynamicUIItem) {
        val data = itemModel.data as? UserMedicalProduct ?: return
        if (!data.isValid()) return
        _isLoading.value = true

        val map = mapOf(
            "status" to UserMedicalProductStatus.TO_RATE.name.lowercase()
        )

        FireStoreManager.buildUserDocRef(FireStoreCollection.USER_MEDICAL_PRODUCTS to data.id)
            .update(map)
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }

    fun rateProduct(itemModel: DynamicUIItem, rate: Int) {
        val data = itemModel.data as? UserMedicalProduct ?: return
        if (!data.isValid()) return
        val id = data.id

        _isLoading.value = true

        val map = mapOf(
            "rate" to rate,
            "is_rated" to true
        )

        FireStoreManager.buildUserDocRef(FireStoreCollection.USER_MEDICAL_PRODUCTS to id)
            .update(map)
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.update(clazz = UserMedicalProduct::class.java, primaryKey = id, data = map)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
}