package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserExercisePlanRepo {
    suspend fun observeUserExercisePlans()
    fun getUserExercisePlansFlow(): Flow<List<UserExercisePlan>>
}

class UserExercisePlanRepoImpl @Inject constructor() : UserExercisePlanRepo {

    override suspend fun observeUserExercisePlans() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_EXERCISE_PLANS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserExercisePlansFlow(): Flow<List<UserExercisePlan>> {
        return RealmManager.flow(
            UserExercisePlan::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserExercisePlanModule {

    @Binds
    abstract fun bindUserExercisePlanRepository(
        impl: UserExercisePlanRepoImpl
    ): UserExercisePlanRepo
}
