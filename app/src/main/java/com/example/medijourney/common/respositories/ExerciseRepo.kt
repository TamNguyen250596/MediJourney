package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Exercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository interface
interface ExerciseRepo {
    suspend fun listenExercises()
    fun getExercisesFlow(): Flow<List<Exercise>>
}

// Repository implementation
class ExerciseRepoImpl @Inject constructor() : ExerciseRepo {

    override suspend fun listenExercises() {
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
                .sort("position", true)
        )
    }
}
