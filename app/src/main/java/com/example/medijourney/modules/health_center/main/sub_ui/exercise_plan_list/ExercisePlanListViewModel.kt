package com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.replaceAllValueOf
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.VideoModel
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class ExercisePlanListViewModel: ViewModel() {

    // Properties
    val userPlanItemStateList = mutableStateListOf<DynamicUIItem>()
    private var userExercisePlanResults: RealmResults<UserExercisePlan>? = null
    private var exerciseResults: RealmResults<Exercise>? = null
    private var exerciseLevelResults: RealmResults<ExerciseLevel>? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            generateUserPlanList(userExercisePlanResults).replaceAllValueOf(userPlanItemStateList)
            observeData()
        }
    }
    
    // Functions
    private suspend fun getData() {
        getUserExercisePlanResults()
        getExerciseResults()
        getExerciseLevelResults()
    }
    
    private suspend fun getUserExercisePlanResults() {
        userExercisePlanResults = RealmManager.read(UserExercisePlan::class.java)
    }

    private suspend fun getExerciseResults() {
        exerciseResults = RealmManager.read(Exercise::class.java, sort = listOf(Pair(Exercise::position.name, Sort.DESCENDING)))
    }

    private suspend fun getExerciseLevelResults() {
        exerciseLevelResults = RealmManager.read(ExerciseLevel::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val userExercisePlans = userExercisePlanResults ?: return
        val exercises = exerciseResults ?: return
        val exerciseLevels = exerciseLevelResults ?: return
        
        combine(
            userExercisePlans.asFlow(),
            exercises.asFlow(),
            exerciseLevels.asFlow(),
        ) { userExercisePlansChanges, exercisesChanges, exerciseLevelsChanges ->
            userExercisePlanResults = userExercisePlansChanges.list
            exerciseResults = exercisesChanges.list
            exerciseLevelResults = exerciseLevelsChanges.list
        }
            .debounce(500)
            .collectLatest {
                generateUserPlanList(userExercisePlanResults).replaceAllValueOf(userPlanItemStateList)
            }
    }
    
    private fun generateUserPlanList(userExercisePlanResults: RealmResults<UserExercisePlan>?): List<DynamicUIItem> {
        val plans = userExercisePlanResults ?: return emptyList()

        return plans.mapNotNull { plan ->
            if (!plan.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = plan.id,
                groupIndex = 0,
                data = plan,
                backgroundColor = 0,
            ).apply {
                plan.name?.let {
                    title = MTextStyle(it)
                }
                description = MTextStyle(plan.duration.toString() + "min")
            }
        }
    }

    fun getVideoModels(itemModel: DynamicUIItem): Array<VideoModel> {
        val default = emptyArray<VideoModel>()
        val userExercisePlan = itemModel.data as? UserExercisePlan ?: return default
        if (!userExercisePlan.isValid()) return default
        if (userExercisePlan.exercises.isEmpty()) return default
        val exercises = exerciseResults ?: return default
        val exerciseLevels = exerciseLevelResults ?: return default

        return userExercisePlan.exercises.mapNotNull { exerciseId ->
            val exercise = exercises.firstOrNull { it.isValid() && it.id == exerciseId } ?: return@mapNotNull null
            val levelId = userExercisePlan.levels[exercise.tag] ?: return@mapNotNull null
            val exerciseLevel = exerciseLevels.firstOrNull { it.isValid() && it.id == levelId } ?: return@mapNotNull null

            VideoModel().apply {
                title = "${exerciseLevel.name} ${exercise.name}"
                videoTag = "exercises/${exercise.tag}_${exerciseLevel.level}.mp4"
            }
        }.toTypedArray()
    }

    fun getUserExercisePlanId(itemModel: DynamicUIItem): String? {
        val userExercisePlan = itemModel.data as? UserExercisePlan ?: return null
        if (!userExercisePlan.isValid()) return null

        return userExercisePlan.id
    }
}