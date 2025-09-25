package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ExerciseLevelRepository {
    suspend fun observeExerciseLevels()
    fun getExerciseLevelsFlow(): Flow<List<ExerciseLevel>>
}

// Repository implementation
class ExerciseLevelRepositoryImpl @Inject constructor() : ExerciseLevelRepository {

    override suspend fun observeExerciseLevels() {
        FireStoreManager.observeCollection(
            FireStoreCollection.EXERCISE_LEVELS,
            queryBuilder = FSQueryBuilder()
                .equalTo("enable", true)
        )
    }

    override fun getExerciseLevelsFlow(): Flow<List<ExerciseLevel>> {
        return RealmManager.flow(
            ExerciseLevel::class,
            queryBuilder = RQueryBuilder()
                .equalTo("enable", true)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class ExerciseLevelModule {

    @Binds
    abstract fun bindExerciseLevelRepository(
        impl: ExerciseLevelRepositoryImpl
    ): ExerciseLevelRepository
}
