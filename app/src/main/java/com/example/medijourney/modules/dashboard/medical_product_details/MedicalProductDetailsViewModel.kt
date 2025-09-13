package com.example.medijourney.modules.dashboard.medical_product_details

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.helpers.CurrencyHelper
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.MedicalProduct
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class MedicalProductDetailsViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _bitmap = MutableStateFlow<Uri?>(null)
    val bitmap: StateFlow<Uri?> = _bitmap.asStateFlow()
    private val _productTitle = MutableStateFlow("")
    val productTitle = _productTitle.asStateFlow()
    private val _htmlDescription = MutableStateFlow("")
    val htmlDescription = _htmlDescription.asStateFlow()
    private val _totalPrice = MutableStateFlow("")
    val totalPrice = _totalPrice.asStateFlow()
    private val _displayPaymentPicker = MutableStateFlow(false)
    val displayPaymentPicker: StateFlow<Boolean> = _displayPaymentPicker.asStateFlow()
    private val _paymentMethodItemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val paymentMethodItemModels: StateFlow<List<DynamicUIItem>> = _paymentMethodItemModels.asStateFlow()
    private var medicalProduct: MedicalProduct? = null
    private var numberOfItems = 0

    // Life cycle
    fun onViewCreated(medicalProductId: String) {
        viewModelScope.launch {
            getMedicalProduct(medicalProductId)
            updateContent(medicalProduct)
            observeMedicalProduct()
        }
        _totalPrice.value = CurrencyHelper.getPrice(0.0, "USD")
    }

    // Functions
    private suspend fun getMedicalProduct(medicalProductId: String) {
        medicalProduct = RealmManager.read(MedicalProduct::class.java, medicalProductId)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeMedicalProduct() {
        val medicalProduct = medicalProduct ?: return

        medicalProduct
            .asFlow()
            .debounce(500)
            .collect {
                this.medicalProduct = it.obj
                updateContent(it.obj)
            }
    }

    private fun updateContent(product: MedicalProduct?) {
        val product = product ?: return
        if (!product.isValid()) return

        fetchImage(product.imageName)
        _productTitle.value = product.name ?: ""
        _htmlDescription.value = product.description ?: ""
    }

    private fun fetchImage(imageName: String?) {
        val imageName = imageName ?: return
        if (imageName.isEmpty()) return
        val link = "images/medical_products/$imageName.png"

        FirebaseStorageManager.downloadImage(link) { downloadedBitmap ->
            _bitmap.value = downloadedBitmap
        }
    }

    // Price
    fun updatePrice(numberOfItems: Int) {
        val medicalProduct = medicalProduct ?: return
        if (!medicalProduct.isValid()) return

        this.numberOfItems = numberOfItems
        CurrencyHelper.getPrice(medicalProduct.price * numberOfItems, "USD").let {
            _totalPrice.value = it
        }
    }

    // Payment Method
    fun updatePaymentMethodPicker(display: Boolean) {
        if (numberOfItems == 0) return
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

    // Buy
    fun buyMedicalProduct(completion: (Boolean) -> Unit) {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return completion(false)
        val medicalProduct = medicalProduct ?: return completion(false)
        if (!medicalProduct.isValid()) return completion(false)
        _isLoading.value = true

        val map: MutableMap<String, Any> = mutableMapOf()
        map["medical_product_id"] = medicalProduct.id
        map["number_of_items"] = numberOfItems
        map["total_price_string"] = totalPrice.value
        map["created_at"] = System.currentTimeMillis()
        map["user_code"] = currentUserCode
        map["is_read"] = false

        viewModelScope.launch {
            val result = FireStoreManager.createDoc(FireStoreCollection.USER_MEDICAL_PRODUCTS, null, map)
            _isLoading.value = false
            completion(result)
        }
    }
}