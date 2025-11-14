package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ExerciseLevelRepo {
    suspend fun listenExerciseLevels()
    fun getExerciseLevelsFlow(): Flow<List<ExerciseLevel>>
}

// Repository implementation
class ExerciseLevelRepoImpl @Inject constructor() : ExerciseLevelRepo {

    override suspend fun listenExerciseLevels() {
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
