package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserRecommendRecipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserRecommendRecipeRepo {
    suspend fun observeUserRecommendRecipes()
    fun getUserRecommendRecipesFlow(): Flow<List<UserRecommendRecipe>>
}

class UserRecommendRecipeRepoImpl @Inject constructor() : UserRecommendRecipeRepo {

    override suspend fun observeUserRecommendRecipes() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_RECOMMEND_RECIPES,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserRecommendRecipesFlow(): Flow<List<UserRecommendRecipe>> {
        return RealmManager.flow(
            UserRecommendRecipe::class,
            queryBuilder = RQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }
}
