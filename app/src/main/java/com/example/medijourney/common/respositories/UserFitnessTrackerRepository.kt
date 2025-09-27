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
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserFitnessTrackerRepository {
    suspend fun observeUserFitnessTrackers()
    fun getUserFitnessTrackerFlow(id: String): Flow<UserFitnessTracker?>
    fun getUserFitnessTrackersFlow(): Flow<List<UserFitnessTracker>>
}

class UserFitnessTrackerRepositoryImpl @Inject constructor() : UserFitnessTrackerRepository {

    override suspend fun observeUserFitnessTrackers() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_FITNESS_TRACKERS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserFitnessTrackerFlow(id: String): Flow<UserFitnessTracker?> {
        return RealmManager.flow(
            UserFitnessTracker::class,
            queryBuilder = RQueryBuilder()
                .equalTo(id, id)
        ).map { it.firstOrNull() }
    }

    override fun getUserFitnessTrackersFlow(): Flow<List<UserFitnessTracker>> {
        return RealmManager.flow(
            UserFitnessTracker::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserFitnessTrackerModule {

    @Binds
    abstract fun bindUserFitnessTrackerRepository(
        impl: UserFitnessTrackerRepositoryImpl
    ): UserFitnessTrackerRepository
}

