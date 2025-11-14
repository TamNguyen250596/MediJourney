package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSleepTrackingReport
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository interface
interface UserSleepTrackingReportRepo {
    suspend fun observeUserSleepTrackingReports(deviceId: String)
    fun getUserSleepTrackingReportsFlow(deviceId: String): Flow<List<UserSleepTrackingReport>>
}

// Repository implementation
class UserSleepTrackingReportRepoImpl @Inject constructor() : UserSleepTrackingReportRepo {

    override suspend fun observeUserSleepTrackingReports(deviceId: String) {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_SLEEP_TRACKING_REPORTS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo("device_id", deviceId)
        )
    }

    override fun getUserSleepTrackingReportsFlow(deviceId: String): Flow<List<UserSleepTrackingReport>> {
        return RealmManager.flow(
            UserSleepTrackingReport::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
                .equalTo(UserSleepTrackingReport::deviceId.name, deviceId)
                .sort(UserSleepTrackingReport::reportedAt.name, Sort.ASCENDING)
        )
    }
}