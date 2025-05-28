package com.example.medijourney.modules.health_center.main.sub_ui.healthy_receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Recipe
import com.example.medijourney.common.models.realm_models.UserRecommendRecipe
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class RecommendedRecipesViewModel: ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val itemModels: StateFlow<List<ImageItemModel>> = _itemModels.asStateFlow()
    private var recipes: RealmResults<Recipe>? = null
    private var userRecommendRecipes: RealmResults<UserRecommendRecipe>? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            _itemModels.value = generateReceiptList(userRecommendRecipes, recipes)
            observeData()
        }
    }

    // Functions
    private suspend fun getData() {
        getReceipts()
        getUserRecommendReceipts()
    }

    private suspend fun getReceipts() {
        recipes = RealmManager.read(Recipe::class.java)
    }

    private suspend fun getUserRecommendReceipts() {
        userRecommendRecipes = RealmManager.read(UserRecommendRecipe::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val receipts = recipes ?: return
        val userRecommendReceipts = userRecommendRecipes ?: return

        combine(
            receipts.asFlow(),
            userRecommendReceipts.asFlow(),
        ) { receiptsChanges, userRecommendReceiptsChanges ->
            this.recipes = receiptsChanges.list
            this.userRecommendRecipes = userRecommendReceiptsChanges.list
        }
            .debounce(500)
            .collectLatest {
                _itemModels.value = generateReceiptList(userRecommendRecipes, recipes)
            }
    }

    @Suppress("NAME_SHADOWING")
    private fun generateReceiptList(userRecommendRecipes: RealmResults<UserRecommendRecipe>?,
                                    recipes: RealmResults<Recipe>?): List<ImageItemModel> {
        val userRecommendRecipes = userRecommendRecipes ?: return listOf()
        val userRecommendRecipe = userRecommendRecipes.firstOrNull() ?: return listOf()
        if (!userRecommendRecipe.isValid()) return listOf()
        val recipes = recipes ?: return listOf()


        return userRecommendRecipe.recommendReceipts.mapNotNull { recipeId ->
            val receipt = recipes.firstOrNull { it.isValid() && it.id == recipeId } ?: return@mapNotNull null

            ImageItemModel(
                itemTag = receipt.tag,
                data = receipt,
                image = ImageStyle(url = "images/recipes/${receipt.imageName}.jpg")
            ).apply {
                receipt.name?.let { name ->
                    title = MTextStyle(name)
                }
            }
        }
    }
}