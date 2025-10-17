package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Hospital
import com.google.firebase.firestore.Filter.arrayContains
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Repository interface
interface HospitalRepo {
    suspend fun observeHospitals(keyword: String?, tagCursor: String?): Flow<List<MutableMap<String, Any>>>
    fun getHospitalsFlow(keyword: String?): Flow<List<Hospital>>
}

// Repository implementation
class HospitalRepoImpl @Inject constructor() : HospitalRepo {

    override suspend fun observeHospitals(keyword: String?, tagCursor: String?): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.HOSPITALS,
            queryBuilder = FSQueryBuilder()
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        arrayContains(Hospital::keywords.name, keyword)
                    }
                    if (tagCursor != null) {
                        greaterThan(Hospital::tag.name, tagCursor)
                        lessThan(Hospital::tag.name, tagCursor)
                    }
                }
                .orderBy(Hospital::tag.name, Query.Direction.ASCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(Hospital(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override fun getHospitalsFlow(keyword: String?): Flow<List<Hospital>> {
        return RealmManager.flow(
            Hospital::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        arrayContains(Hospital::keywords.name, keyword)
                    }
                }
                .sort(Hospital::tag.name, Sort.ASCENDING)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class HospitalModule {

    @Binds
    abstract fun bindHospitalRepository(
        impl: HospitalRepoImpl
    ): HospitalRepo
}