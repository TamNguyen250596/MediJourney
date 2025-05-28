package com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.UserRecommendExercise
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class RecommendedExerciseViewModel: ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val itemModels: StateFlow<List<ImageItemModel>> = _itemModels.asStateFlow()
    private var exercises: RealmResults<Exercise>? = null
    private var userRecommendExercises: RealmResults<UserRecommendExercise>? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            _itemModels.value = generateExerciseList()
            observeData()
        }
    }

    // Functions
    private suspend fun getData() {
        getExercises()
        getUserRecommendExercises()
    }

    private suspend fun getExercises() {
        exercises = RealmManager.read(Exercise::class.java, sort = listOf(Pair(Exercise::position.name, Sort.ASCENDING)))
    }

    private suspend fun getUserRecommendExercises() {
        userRecommendExercises = RealmManager.read(UserRecommendExercise::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val exercises = exercises ?: return
        val userRecommendExercises = userRecommendExercises ?: return

        combine(
            exercises.asFlow(),
            userRecommendExercises.asFlow(),
        ) { exercisesChanges, userRecommendExercisesChanges ->
            this.exercises = exercisesChanges.list
            this.userRecommendExercises = userRecommendExercisesChanges.list
        }
            .debounce(500)
            .collectLatest {
                _itemModels.value = generateExerciseList()
            }
    }

    private fun generateExerciseList(): List<ImageItemModel> {
        val userRecommendExercises = userRecommendExercises ?: return listOf()
        val userRecommendExercise = userRecommendExercises.firstOrNull() ?: return listOf()
        if (!userRecommendExercise.isValid()) return listOf()
        val exercises = exercises ?: return listOf()

        return exercises.mapNotNull { exercise ->
            if (!exercise.isValid()) return@mapNotNull null
            if (!userRecommendExercise.recommendExercises.contains(exercise.id)) return@mapNotNull null

            ImageItemModel(
                itemTag = exercise.tag,
                data = exercise,
                image = ImageStyle(url = "images/exercises/${exercise.imageName}.jpg")
            ).apply {
                exercise.name?.let { name ->
                    title = MTextStyle(name)
                }
            }
        }
    }
}