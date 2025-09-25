package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Exercise
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository interface
interface ExerciseRepository {
    suspend fun observeExercises()
    fun getExercisesFlow(): Flow<List<Exercise>>
}

// Repository implementation
class ExerciseRepositoryImpl @Inject constructor() : ExerciseRepository {

    override suspend fun observeExercises() {
        FireStoreManager.observeCollection(
            FireStoreCollection.EXERCISES,
            queryBuilder = FSQueryBuilder()
                .equalTo("enable", true)
        )
    }

    override fun getExercisesFlow(): Flow<List<Exercise>> {
        return RealmManager.flow(
            Exercise::class,
            queryBuilder = RQueryBuilder()
                .equalTo("enable", true)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class ExerciseModule {

    @Binds
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository
}
