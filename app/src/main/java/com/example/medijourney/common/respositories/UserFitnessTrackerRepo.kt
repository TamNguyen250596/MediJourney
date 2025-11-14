package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserFitnessTrackerRepo {
    suspend fun createUserFitnessTracker(data: Map<String, Any>): Boolean
    suspend fun listenUserFitnessTrackers()
    fun getUserFitnessTrackerFlow(id: String): Flow<UserFitnessTracker?>
    fun getUserFitnessTrackersFlow(): Flow<List<UserFitnessTracker>>
    suspend fun updateUserFitnessTracker(id: String, data: Map<String, Any>): Boolean
    suspend fun deleteUserFitnessTracker(id: String): Boolean
}

class UserFitnessTrackerRepoImpl @Inject constructor() : UserFitnessTrackerRepo {
    override suspend fun createUserFitnessTracker(data: Map<String, Any>): Boolean {
        return FireStoreManager.createDoc(
            FireStoreCollection.USER_FITNESS_TRACKERS,
            data = data
        )
    }

    override suspend fun listenUserFitnessTrackers() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_FITNESS_TRACKERS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserFitnessTrackerFlow(id: String): Flow<UserFitnessTracker?> {
        return RealmManager.flow(UserFitnessTracker::class, id)
    }

    override fun getUserFitnessTrackersFlow(): Flow<List<UserFitnessTracker>> {
        return RealmManager.flow(
            UserFitnessTracker::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override suspend fun updateUserFitnessTracker(
        id: String,
        data: Map<String, Any>
    ): Boolean {
        return FireStoreManager.updateDoc(
            FireStoreCollection.USER_FITNESS_TRACKERS,
            id,
            data
        )
    }

    override suspend fun deleteUserFitnessTracker(id: String): Boolean {
        return FireStoreManager.deleteDoc(
            FireStoreCollection.USER_FITNESS_TRACKERS,
            id
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserFitnessTrackerModule {

    @Binds
    abstract fun bindUserFitnessTrackerRepository(
        impl: UserFitnessTrackerRepoImpl
    ): UserFitnessTrackerRepo
}

