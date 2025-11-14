package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserRecommendExercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository interface
interface UserRecommendExerciseRepo {
    suspend fun observeUserRecommendExercises()
    fun getUserRecommendExercisesFlow(): Flow<List<UserRecommendExercise>>
}

// Repository implementation
class UserRecommendExerciseRepoImpl @Inject constructor() : UserRecommendExerciseRepo {

    override suspend fun observeUserRecommendExercises() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_RECOMMEND_EXERCISE,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserRecommendExercisesFlow(): Flow<List<UserRecommendExercise>> {
        return RealmManager.flow(
            UserRecommendExercise::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }
}
