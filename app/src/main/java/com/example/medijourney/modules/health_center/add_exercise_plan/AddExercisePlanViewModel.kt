package com.example.medijourney.modules.health_center.add_exercise_plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ExerciseLevelRepo
import com.example.medijourney.common.respositories.ExerciseRepo
import com.example.medijourney.common.respositories.UserExercisePlanRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExercisePlanViewModel @Inject constructor(
    saveStateHandle: SavedStateHandle,
    exerciseRepo: ExerciseRepo,
    exerciseLevelRepo: ExerciseLevelRepo,
    private val userExercisePlanRepo: UserExercisePlanRepo
) : ViewModel() {

    // Properties
    private val _planNameErrorMessageId = MutableStateFlow<Int?>(null)
    val planNameErrorMessageId: StateFlow<Int?> = _planNameErrorMessageId.asStateFlow()
    private val _exerciseSequenceStateList = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val exerciseSequenceStateList: StateFlow<List<DynamicUIItem>> = _exerciseSequenceStateList.asStateFlow()
    private val _exercisesStateList = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val exercisesStateList: StateFlow<List<DynamicUIItem>> = _exercisesStateList.asStateFlow()
    private val _durationState = MutableStateFlow(0)
    val durationState: StateFlow<Int> = _durationState.asStateFlow()
    private val _enableAddExerciseButton = MutableStateFlow(false)
    val enableAddExerciseButton: StateFlow<Boolean> = _enableAddExerciseButton.asStateFlow()
    private val _planeName = MutableStateFlow("")
    val planeName: StateFlow<String> = _planeName.asStateFlow()
    private val userExercisePlanId = saveStateHandle.get<String>("userExercisePlanId")
    private val exercisesFlow = exerciseRepo.getExercisesFlow()
    private val exerciseLevelsFlow = exerciseLevelRepo.getExerciseLevelsFlow()
    private var userExercisePlanFlow = userExercisePlanRepo.getUserExercisePlanFlow(userExercisePlanId ?: "")

    // Companion
    companion object {
        const val LEVELS_KEY = "levels"
        const val SELECTED_LEVEL_INDEX_KEY = "selectedLevelIndex"
        const val IS_EXERCISE_SELECTED_KEY = "isExerciseSelected"
        private const val SELECTED_EXERCISE_KEY = "selectedExercise"
        private const val SELECTED_LEVEL_KEY = "selectedLevel"
    }

    // Lifecycle
    init {
        viewModelScope.launch {
            updateAvailableInfo()
            observeData()
        }
    }

    // Functions
    private suspend fun updateAvailableInfo() {
        val userExercisePlan = userExercisePlanFlow.first() ?: return
        if (!userExercisePlan.isValid()) return

        _planeName.value = userExercisePlan.name ?: ""
        _durationState.value = userExercisePlan.duration.toInt()
        _enableAddExerciseButton.value = true
    }

    private suspend fun observeData() {
        combine(
            userExercisePlanFlow,
            exercisesFlow,
            exerciseLevelsFlow
        ) { userExercisePlan, exercises, exerciseLevels ->
            Triple(userExercisePlan, exercises, exerciseLevels)
        }
            .firstThenDebounce(500)
            .collectLatest {
                _exerciseSequenceStateList.value = generateExerciseSequenceList(
                    it.first,
                    it.second,
                    it.third
                )
                _exercisesStateList.value = generateExerciseList(
                    it.second,
                    it.third
                )
            }
    }

    private fun generateExerciseSequenceList(
        userExercisePlan: UserExercisePlan?,
        exercises: List<Exercise>,
        exerciseLevels: List<ExerciseLevel>
    ): List<DynamicUIItem> {

        val defaultList = MutableList(4) {
            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.toString(),
                backgroundColor = 0
            )
        }
        val userExercisePlan = userExercisePlan ?: return defaultList
        if (!userExercisePlan.isValid()) return defaultList
        val exerciseIds = userExercisePlan.exercises
        val levels = userExercisePlan.levels

        return defaultList.mapIndexed { index, dynamicUIItem ->
            if (index >= exerciseIds.size) return@mapIndexed dynamicUIItem
            val exerciseId = exerciseIds[index]
            val exercise = exercises.firstOrNull { it.id == exerciseId } ?: return@mapIndexed dynamicUIItem
            if (!exercise.isValid()) return@mapIndexed dynamicUIItem
            val levelId = levels[exercise.tag] ?: return@mapIndexed dynamicUIItem
            val level = exerciseLevels.firstOrNull { it.id == levelId } ?: return@mapIndexed dynamicUIItem
            if (!level.isValid()) return@mapIndexed dynamicUIItem

            dynamicUIItem.copy(
                image = ImageStyle(url = "images/exercises/${exercise.imageName}.jpg"),
                title = MTextStyle("${level.name} ${exercise.name}"),
                additionalData = mapOf(
                    SELECTED_EXERCISE_KEY to exercise,
                    SELECTED_LEVEL_KEY to level,
                    Constants.IS_VALID to true
                )
            )
        }
    }

    fun updatePlanName(value: String) {
        _planNameErrorMessageId.value = if (value.isEmpty()) {
            R.string.the_field_is_required
        } else {
            null
        }
        _planeName.value = value
        enableButton()
    }

    private fun generateExerciseList(
        exercises: List<Exercise>,
        exerciseLevels: List<ExerciseLevel>
    ): List<DynamicUIItem> {
        return exercises.mapNotNull { exercise ->
            if (!exercise.isValid()) return@mapNotNull null

            val levelItems = exercise.levels.mapNotNull {
                val level = exerciseLevels.firstOrNull { level -> level.isValid() && level.id == it } ?: return@mapNotNull null

                DynamicUIItem(
                    type = Constants.ITEM,
                    itemTag = exercise.tag + level.level,
                    backgroundColor = 0,
                    data = level
                ).apply {
                    level.name?.let {
                        title = MTextStyle(it)
                    }
                }
            }

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = exercise.tag,
                backgroundColor = 0,
                data = exercise
            ).apply {
                exercise.name?.let {
                    title = MTextStyle(it)
                }
                exercise.description?.let {
                    description = MTextStyle(it)
                }
                exercise.imageName?.let {
                    image = ImageStyle(url = "images/exercises/${it}.jpg")
                }
                additionalData = mapOf(LEVELS_KEY to levelItems)
            }
        }
    }

    fun updateExerciseSequenceItem(exercise: DynamicUIItem, level: DynamicUIItem, index: Int) {
        val list = _exerciseSequenceStateList.value.toMutableList()
        if (index >= list.size) return

        (level.data as? ExerciseLevel)?.let {
            if (!it.isValid()) return
            _durationState.value += it.duration.toInt()
        }

        _exerciseSequenceStateList.update {
            it.toMutableList().also { mutableList ->
                mutableList[index] = mutableList[index].copy(
                    image = ImageStyle(url = exercise.image?.url),
                    title = MTextStyle("${level.title?.text} ${exercise.title?.text}"),
                    additionalData = mapOf(
                        SELECTED_EXERCISE_KEY to exercise.data,
                        SELECTED_LEVEL_KEY to level.data,
                        Constants.IS_VALID to true
                    )
                )
            }
        }
        enableButton()
    }

    fun removeExerciseSequenceItem(index: Int) {
        val list = _exerciseSequenceStateList.value.toMutableList()
        if (index >= list.size) return

        val additionalData = list[index].additionalData as? Map<*, *>
        val level = additionalData?.get(SELECTED_LEVEL_KEY) as? ExerciseLevel
        if (level != null && level.isValid()) {
            _durationState.value -= level.duration.toInt()
        }

        _exerciseSequenceStateList.update {
            it.toMutableList().also { mutableList ->
                mutableList[index] = mutableList[index].copy(
                    image = null,
                    title = null,
                    additionalData = null
                )
            }
        }
        enableButton()
    }

    private fun enableButton() {
        _enableAddExerciseButton.value = _planeName.value.isNotEmpty() &&
                _exerciseSequenceStateList.value.any {
                    val additionalData = it.additionalData as? Map<*, *> ?: return@any false
                    additionalData[Constants.IS_VALID] == true
                }
    }

    fun saveUserPlanExercise(completion: (Boolean) -> Unit) {
        viewModelScope.launch {
            val map = mutableMapOf<String, Any>()
            map["name"] = _planeName.value

            val exerciseIds = mutableListOf<Int>()
            val levels = mutableMapOf<String, Int>()

            _exerciseSequenceStateList.value.forEach {
                val additionalData = it.additionalData as? Map<*, *> ?: return@forEach
                val exercise = additionalData[SELECTED_EXERCISE_KEY] as? Exercise ?: return@forEach
                val level = additionalData[SELECTED_LEVEL_KEY] as? ExerciseLevel ?: return@forEach
                if (exercise.isValid() && level.isValid()) {
                    exerciseIds.add(exercise.id)
                    levels[exercise.tag] = level.id
                }
            }
            map["duration"] = _durationState.value
            map["exercises"] = exerciseIds
            map["levels"] = levels

            var result = false
            userExercisePlanId?.let {
                result = userExercisePlanRepo.updateUserExercisePlan(it, map)
            } ?: run {
                result = userExercisePlanRepo.createUserExercisePlan(map)
            }
            completion.invoke(result)
        }
    }
}