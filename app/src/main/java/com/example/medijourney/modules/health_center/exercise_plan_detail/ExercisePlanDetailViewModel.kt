package com.example.medijourney.modules.health_center.exercise_plan_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.asFlow
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

class ExercisePlanDetailViewModel : ViewModel() {

    // Properties
    private val _planInfoList = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val planInfoList: StateFlow<List<DynamicUIItem>> = _planInfoList.asStateFlow()
    private var userExercisePlan: UserExercisePlan? = null
    private var exerciseResults: RealmResults<Exercise>? = null
    private var exerciseLevelResults: RealmResults<ExerciseLevel>? = null

    // Life cycle
    fun onViewCreated(userExercisePlanId: String) {
        viewModelScope.launch {
            getData(userExercisePlanId)
            _planInfoList.value = generatePlanInfoItemModels(userExercisePlan, exerciseResults, exerciseLevelResults)
            observeData()
        }
    }

    // Functions
    private suspend fun getData(userExercisePlanId: String) {
        getUserExercisePlanResults(userExercisePlanId)
        getExerciseResults()
        getExerciseLevelResults()
    }

    private suspend fun getUserExercisePlanResults(userExercisePlanId: String) {
        userExercisePlan = RealmManager.read(UserExercisePlan::class.java, userExercisePlanId)
    }

    private suspend fun getExerciseResults() {
        exerciseResults = RealmManager.read(Exercise::class.java, sort = listOf(Pair(Exercise::position.name, Sort.DESCENDING)))
    }

    private suspend fun getExerciseLevelResults() {
        exerciseLevelResults = RealmManager.read(ExerciseLevel::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val userExercisePlan = userExercisePlan ?: return
        val exercises = exerciseResults ?: return
        val exerciseLevels = exerciseLevelResults ?: return

        combine(
            userExercisePlan.asFlow(),
            exercises.asFlow(),
            exerciseLevels.asFlow(),
        ) { userExercisePlansChanges, exercisesChanges, exerciseLevelsChanges ->
            this.userExercisePlan = userExercisePlansChanges.obj
            exerciseResults = exercisesChanges.list
            exerciseLevelResults = exerciseLevelsChanges.list
        }
            .debounce(500)
            .collectLatest {
                _planInfoList.value = generatePlanInfoItemModels(userExercisePlan, exerciseResults, exerciseLevelResults)
            }
    }

    private fun generatePlanInfoItemModels(userExercisePlan: UserExercisePlan?,
                                           exerciseResults: RealmResults<Exercise>?,
                                           exerciseLevelResults: RealmResults<ExerciseLevel>?): List<DynamicUIItem> {
        if (userExercisePlan == null) return emptyList()
        if (!userExercisePlan.isValid()) return emptyList()
        if (userExercisePlan.exercises.isEmpty()) return emptyList()
        val exercises = exerciseResults ?: return emptyList()
        val exerciseLevels = exerciseLevelResults ?: return emptyList()

        return userExercisePlan.exercises.mapNotNull { exerciseId ->
            val exercise = exercises.firstOrNull { it.isValid() && it.id == exerciseId } ?: return@mapNotNull null
            val levelId = userExercisePlan.levels[exercise.tag] ?: return@mapNotNull null
            val exerciseLevel = exerciseLevels.firstOrNull { it.isValid() && it.id == levelId } ?: return@mapNotNull null

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
        val userExercisePlan = userExercisePlan ?: return completion(false)
        if (!userExercisePlan.isValid()) return completion(false)

        FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_EXERCISE_PLANS, userExercisePlan.id))
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.delete(userExercisePlan)
                    completion(true)
                }
            }
            .addOnFailureListener {
                completion(false)
            }
    }
}