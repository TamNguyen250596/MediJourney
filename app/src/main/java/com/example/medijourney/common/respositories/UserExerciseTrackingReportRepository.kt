package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserExerciseTrackingReport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserExerciseTrackingReportRepository {
    suspend fun observeUserExerciseTrackingReports(deviceId: String)
    fun getUserExerciseTrackingReportsFlow(deviceId: String): Flow<List<UserExerciseTrackingReport>>
}

class UserExerciseTrackingReportRepositoryImpl @Inject constructor() : UserExerciseTrackingReportRepository {

    override suspend fun observeUserExerciseTrackingReports(deviceId: String) {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_EXERCISE_TRACKING_REPORTS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo("device_id", deviceId)
        )
    }

    override fun getUserExerciseTrackingReportsFlow(deviceId: String): Flow<List<UserExerciseTrackingReport>> {
        return RealmManager.flow(
            UserExerciseTrackingReport::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo(UserExerciseTrackingReport::deviceId.name, deviceId)
                .sort(UserExerciseTrackingReport::reportedAt.name, Sort.ASCENDING)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class UserExerciseTrackingReportModule {

    @Binds
    abstract fun bindUserExerciseTrackingReportRepository(
        impl: UserExerciseTrackingReportRepositoryImpl
    ): UserExerciseTrackingReportRepository
}
