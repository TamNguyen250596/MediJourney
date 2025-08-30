package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getInt
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
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

    override fun update(map: Map<String, Any>) {
        totalPriceString = map["total_price_string"] as? String ?: totalPriceString
        rate = map.getInt("rate", rate)
        isRated = map["is_rated"] as? Boolean ?: isRated
        isRead = map["is_read"] as? Boolean ?: isRead
        status = map["status"] as? String ?: status
    }

    override fun didInit(map: Map<String, Any>) {
        super.didInit(map)
        handleToSaveMedicalProduct(map)
    }

    private fun handleToSaveMedicalProduct(map: Map<String, Any>) {
        val medicalProductId = map["medical_product_id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            RealmManager.createRealm().write {
                val medicalProduct = query(MedicalProduct::class, "${MedicalProduct::id.name} == $0", medicalProductId).find().firstOrNull()
                query(
                    UserMedicalProduct::class,
                    "${UserMedicalProduct::medicalProductId.name} == $0 AND ${UserMedicalProduct::medicalProduct.name} == $1", medicalProductId, null)
                    .find()
                    .forEach { it.medicalProduct = medicalProduct }
            }
        }

        FireStoreManager.buildDoc(FireStoreCollection.MEDICAL_PRODUCTS to medicalProductId)
            .addListener {
                val data = it.data
                if (data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RealmManager.createRealm().write {
                            val existingMedicalProduct = query(MedicalProduct::class, "${MedicalProduct::id.name} == $0", medicalProductId).find().firstOrNull()
                            if (existingMedicalProduct == null) {
                                val medicalProduct = MedicalProduct().create(data) as? MedicalProduct ?: return@write
                                query(
                                    UserMedicalProduct::class,
                                    "${UserMedicalProduct::medicalProductId.name} == $0 AND ${UserMedicalProduct::medicalProduct.name} == $1", medicalProductId, null)
                                    .find()
                                    .forEach { it.medicalProduct = copyToRealm(medicalProduct) }
                            } else {
                                existingMedicalProduct.update(data)
                            }
                        }
                    }
                }
            }
    }
}

enum class UserMedicalProductStatus() {
    TO_PAY,
    TO_SHIP,
    TO_RECEIVE,
    TO_RATE
}