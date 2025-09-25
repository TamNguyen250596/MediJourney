package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Recipe
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface RecipeRepository {
    suspend fun observeRecipes()
    fun getRecipesFlow(): Flow<List<Recipe>>
}

class RecipeRepositoryImpl @Inject constructor() : RecipeRepository {

    override suspend fun observeRecipes() {
        FireStoreManager.observeCollection(
            FireStoreCollection.RECIPES,
            queryBuilder = FSQueryBuilder()
                .equalTo("enable", true)
        )
    }

    override fun getRecipesFlow(): Flow<List<Recipe>> {
        return RealmManager.flow(
            Recipe::class,
            queryBuilder = RQueryBuilder()
                .equalTo("enable", true)
        )
    }
}

// Hilt module
@Module
@InstallIn(ViewModelComponent::class)
abstract class RecipeModule {

    @Binds
    abstract fun bindRecipeRepository(
        impl: RecipeRepositoryImpl
    ): RecipeRepository
}
