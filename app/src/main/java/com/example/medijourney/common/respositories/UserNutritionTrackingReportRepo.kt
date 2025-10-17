package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserNutritionTrackingReport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository interface
interface UserNutritionTrackingReportRepo {
    suspend fun observeUserNutritionTrackingReports(deviceId: String)
    fun getUserNutritionTrackingReportsFlow(deviceId: String): Flow<List<UserNutritionTrackingReport>>
}

// Repository implementation
class UserNutritionTrackingReportRepoImpl @Inject constructor() : UserNutritionTrackingReportRepo {

    override suspend fun observeUserNutritionTrackingReports(deviceId: String) {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_NUTRITION_TRACKING_REPORTS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo("device_id", deviceId)
        )
    }

    override fun getUserNutritionTrackingReportsFlow(deviceId: String): Flow<List<UserNutritionTrackingReport>> {
        return RealmManager.flow(
            UserNutritionTrackingReport::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo(UserNutritionTrackingReport::deviceId.name, deviceId)
                .sort(UserNutritionTrackingReport::reportedAt.name, Sort.ASCENDING)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class UserNutritionTrackingReportModule {

    @Binds
    abstract fun bindUserNutritionTrackingReportRepository(
        impl: UserNutritionTrackingReportRepoImpl
    ): UserNutritionTrackingReportRepo
}
