package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMedicalProduct: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var medicalProductId: String? = null
    var medicalProduct: MedicalProduct? = null
    var numberOfItems: Int = 0
    var totalPriceString: String? = null
    var createdAt: RealmInstant? = null
    var userId: String = ""
    var rate: Int = 0
    var isRated: Boolean = false
    var isRead: Boolean = false
    var status: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserMedicalProduct().apply {
            id = map["id"] as? String ?: id
            medicalProductId = map["medical_product_id"] as? String
            numberOfItems = map.getInt("number_of_items")
            totalPriceString = map["total_price_string"] as? String
            createdAt = map.getRealmInstant("created_at")
            userId = map["user_id"] as? String ?: userId
            rate = map.getInt("rate")
            isRated = map["is_rated"] as? Boolean ?: isRated
            isRead = map["is_read"] as? Boolean ?: isRead
            status = map["status"] as? String
        }
    }

    override fun update(map: Map<String, Any>) {}

    override fun setUpAfterCreation(map: Map<String, Any>) {
        super.setUpAfterCreation(map)
        handleMedicalProduct(map)
    }

    private fun handleMedicalProduct(map: Map<String, Any>) {
        if (!isValid()) return
        if (medicalProduct != null) return
        val medicalProductId = map["medical_product_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            launch {
                FireStoreManager.observeDoc(FireStoreCollection.MEDICAL_PRODUCTS, medicalProductId)
            }
            launch {
                RealmManager.link(
                    medicalProductId,
                    MedicalProduct::class,
                    id,
                    UserMedicalProduct::class,
                    UserMedicalProduct::medicalProduct
                )
            }
        }
    }
}

// UserMedicalProductStatus
enum class UserMedicalProductStatus() {
    TO_PAY,
    TO_SHIP,
    TO_RECEIVE,
    TO_RATE
}