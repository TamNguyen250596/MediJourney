package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserExercisePlanRepo {
    suspend fun createUserExercisePlan(data: Map<String, Any>): Boolean
    suspend fun listenUserExercisePlans()
    fun getUserExercisePlanFlow(id: String): Flow<UserExercisePlan?>
    fun getUserExercisePlansFlow(): Flow<List<UserExercisePlan>>
    suspend fun updateUserExercisePlan(id: String, data: Map<String, Any>): Boolean
    suspend fun deleteUserExercisePlan(id: String): Boolean
}

class UserExercisePlanRepoImpl @Inject constructor() : UserExercisePlanRepo {
    override suspend fun createUserExercisePlan(data: Map<String, Any>): Boolean {
        return FireStoreManager.createDoc(FireStoreCollection.USER_EXERCISE_PLANS, data = data)
    }

    override suspend fun listenUserExercisePlans() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_EXERCISE_PLANS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserExercisePlanFlow(id: String): Flow<UserExercisePlan?> {
        return RealmManager.flow(UserExercisePlan::class, id)
    }

    override fun getUserExercisePlansFlow(): Flow<List<UserExercisePlan>> {
        return RealmManager.flow(
            UserExercisePlan::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override suspend fun updateUserExercisePlan(
        id: String,
        data: Map<String, Any>
    ): Boolean {
        return FireStoreManager.updateDoc(FireStoreCollection.USER_EXERCISE_PLANS, id, data)
    }

    override suspend fun deleteUserExercisePlan(id: String): Boolean {
        return FireStoreManager.deleteDoc(FireStoreCollection.USER_EXERCISE_PLANS, id)
    }
}
