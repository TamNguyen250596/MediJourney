package com.example.medijourney.modules.health_center.select_exercise_level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.VideoModel
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SelectExerciseLevelViewModel : ViewModel() {

    // Properties
    var levelItemModels: MutableStateFlow<List<TitleItemModel>> = MutableStateFlow(emptyList())
    private var exercise: Exercise? = null
    private var exerciseLevels: RealmResults<ExerciseLevel>? = null

    // Life cycle
    fun onViewCreated(exerciseId: Int) {
        viewModelScope.launch {
            getData(exerciseId)
            handleExerciseLevels()
            observeData()
        }
    }

    // Functions
    private suspend fun getData(exerciseId: Int) {
        exercise = RealmManager.read(Exercise::class.java, exerciseId)
        exerciseLevels = RealmManager.read(ExerciseLevel::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val exercise = exercise ?: return
        val exerciseLevels = exerciseLevels ?: return

        combine(
            exercise.asFlow(),
            exerciseLevels.asFlow(),
        )
        { exerciseChange, exerciseLevelsChanges ->
            this.exercise = exerciseChange.obj
            this.exerciseLevels = exerciseLevelsChanges.list
        }
            .debounce(500)
            .collectLatest {
                handleExerciseLevels()
            }
    }

    private fun handleExerciseLevels() {
        val exercise = exercise ?: return
        if (!exercise.isValid()) return
        val exerciseLevels = exerciseLevels ?: return

        val items = exercise.levels.map { level ->
            val exerciseLevel = exerciseLevels.find { it.id == level } ?: return
            TitleItemModel(
                itemTag = exerciseLevel.level,
                data = exerciseLevel,
                title = MTextStyle(exerciseLevel.name ?: "")
            )
        }
        levelItemModels.value = items.toMutableList()
    }

    fun getVideoModels(item: TitleItemModel): Array<VideoModel> {
        val default = emptyArray<VideoModel>()
        val exerciseLevel = item.data as? ExerciseLevel ?: return default
        if (!exerciseLevel.isValid()) return default
        val exercise = exercise ?: return default
        if (!exercise.isValid()) return default
        val exerciseTag = exercise.tag
        val level = exerciseLevel.level
        val array = mutableListOf<VideoModel>()
        val videoModel = VideoModel().apply {
            title = exercise.name
            videoTag = "exercises/${exerciseTag}_$level.mp4"
        }

        array.add(videoModel)
        return array.toTypedArray()
    }
}