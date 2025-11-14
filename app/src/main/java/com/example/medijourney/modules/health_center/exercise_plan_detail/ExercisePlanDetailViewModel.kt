package com.example.medijourney.modules.health_center.exercise_plan_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.UserExercisePlan
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisePlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userExercisePlanRepo: UserExercisePlanRepo,
    exerciseRepo: ExerciseRepo,
    exerciseLevelRepo: ExerciseLevelRepo
) : ViewModel() {

    // Properties
    private val _planInfoList = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val planInfoList: StateFlow<List<DynamicUIItem>> = _planInfoList.asStateFlow()
    private val userExercisePlanId = savedStateHandle.get<String>("userExercisePlanId") ?: ""
    private val userExercisePlanFlow = userExercisePlanRepo.getUserExercisePlanFlow(userExercisePlanId)
    private val exercisesFlow = exerciseRepo.getExercisesFlow()
    private val exerciseLevelsFlow = exerciseLevelRepo.getExerciseLevelsFlow()

    // Life cycle
    init {
        viewModelScope.launch {
            observeData()
        }
    }

    // Functions
    private suspend fun observeData() {
        combine(
            userExercisePlanFlow,
            exercisesFlow,
            exerciseLevelsFlow,
        ) { userExercisePlan, exercises, exerciseLevels ->
            if (userExercisePlan == null) return@combine null
            if (!userExercisePlan.isValid()) return@combine null
            if (userExercisePlan.exercises.isEmpty()) return@combine null

            Triple(userExercisePlan, exercises, exerciseLevels)
        }
            .mapNotNull { it }
            .firstThenDebounce(500)
            .collectLatest {
                _planInfoList.value = generatePlanInfoItemModels(
                    it.first,
                    it.second,
                    it.third
                )
            }
    }

    private fun generatePlanInfoItemModels(
        userExercisePlan: UserExercisePlan,
        exercisesList: List<Exercise>,
        exerciseLevelsList: List<ExerciseLevel>)
    : List<DynamicUIItem> {
        return userExercisePlan.exercises.mapNotNull { exerciseId ->
            val exercise = exercisesList.firstOrNull { it.isValid() && it.id == exerciseId } ?: return@mapNotNull null
            val levelId = userExercisePlan.levels[exercise.tag] ?: return@mapNotNull null
            val exerciseLevel = exerciseLevelsList.firstOrNull { it.isValid() && it.id == levelId } ?: return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = "",
                groupIndex = 0,
                data = null,
                backgroundColor = 0
            ).apply {
                exercise.name?.let {
                    title = MTextStyle(it)
                }
                description = MTextStyle(exerciseLevel.duration.toString() + "min")
            }
        }
    }

    fun deleteUserExercisePlanId(completion: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = userExercisePlanRepo.deleteUserExercisePlan(userExercisePlanId)
            completion(result)
        }
    }
}