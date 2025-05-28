package com.example.medijourney.modules.health_center.recipe_video

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Recipe
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
class RecipeVideoViewModel : ViewModel() {

    // Properties
    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()
    private val _htmlDescription = MutableStateFlow("")
    val htmlDescription = _htmlDescription.asStateFlow()
    private var recipe: Recipe? = null

    // Life cycle
    fun onViewCreated(recipeId: Int) {
        viewModelScope.launch {
            getData(recipeId)
            _mediaItems.value = generateMediaItems(recipe)
            _htmlDescription.value = getHtmlString(recipe)
            observeData()
        }
    }

    // Functions
    private suspend fun getData(recipeId: Int) {
        recipe = RealmManager.read(Recipe::class.java, recipeId)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val recipe = recipe ?: return

        recipe
            .asFlow()
            .debounce(500)
            .collect {
                this.recipe = it.obj
                _mediaItems.value = generateMediaItems(this.recipe)
                _htmlDescription.value = getHtmlString(this.recipe)
            }
    }

    @Suppress("NAME_SHADOWING")
    private fun generateMediaItems(recipe: Recipe?): List<MediaItem> {
        val recipe = recipe ?: return emptyList()
        if (!recipe.isValid()) return emptyList()

        return listOf(
            MediaItem.Builder().apply {
                setMediaId("recipes/${recipe.tag}.mp4")
                setMediaMetadata(
                    MediaMetadata.Builder().apply {
                        setUri("")
                        setDisplayTitle(recipe.name)
                    }.build()
                )
            }.build()
        )
    }

    @Suppress("NAME_SHADOWING")
    private fun getHtmlString(recipe: Recipe?): String {
        val recipe = recipe ?: return ""
        if (!recipe.isValid()) return ""

        return recipe.nutritionDescription ?: ""
    }
}