package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.FitnessTrackerActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface FitnessTrackerActivityRepo {
    suspend fun listenFitnessTrackerActivities()
    fun getFitnessTrackerActivitiesFlow(): Flow<List<FitnessTrackerActivity>>
}

// Repository implementation
class FitnessTrackerActivityRepoImpl @Inject constructor() : FitnessTrackerActivityRepo {

    override suspend fun listenFitnessTrackerActivities() {
        FireStoreManager.observeCollection(
            FireStoreCollection.FITNESS_TRACKER_ACTIVITIES,
            queryBuilder = FSQueryBuilder()
                .equalTo("enable", true)
        )
    }

    override fun getFitnessTrackerActivitiesFlow(): Flow<List<FitnessTrackerActivity>> {
        return RealmManager.flow(
            FitnessTrackerActivity::class,
            queryBuilder = RQueryBuilder()
                .equalTo("enable", true)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class FitnessTrackerActivityModule {

    @Binds
    abstract fun bindFitnessTrackerActivityRepository(
        impl: FitnessTrackerActivityRepoImpl
    ): FitnessTrackerActivityRepo
}
