package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.models.realm_models.MedicalProduct
import com.google.firebase.firestore.Filter.arrayContains
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import io.realm.kotlin.query.Sort
import javax.inject.Inject

// Repository interface
interface MedicalProductRepo {
    suspend fun observeMedicalProducts(keyword: String?, positionCursor: Int?): Flow<List<MutableMap<String, Any>>>
    fun getMedicalProductsFlow(keyword: String?): Flow<List<MedicalProduct>>
}

// Repository implementation
class MedicalProductRepoImpl @Inject constructor() : MedicalProductRepo {

    override suspend fun observeMedicalProducts(keyword: String?, positionCursor: Int?): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.MEDICAL_PRODUCTS,
            queryBuilder = FSQueryBuilder()
                .apply {
                    if (keyword.isNullOrEmpty()) {
                        arrayContains(MedicalProduct::keywords.name, keyword)
                    }
                    if (positionCursor != null) {
                        greaterThan(MedicalProduct::position.name, positionCursor)
                        lessThan(MedicalProduct::position.name, positionCursor)
                    }
                }
                .orderBy(MedicalProduct::position.name, Query.Direction.ASCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(MedicalProduct(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override fun getMedicalProductsFlow(keyword: String?): Flow<List<MedicalProduct>> {
        return RealmManager.flow(
            MedicalProduct::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (keyword.isNullOrEmpty()) {
                        arrayContains(MedicalProduct::keywords.name, keyword)
                    }
                }
                .sort(MedicalProduct::position.name, Sort.ASCENDING)
        )
    }
}
