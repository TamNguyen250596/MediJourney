package com.example.medijourney.modules.health_center.add_exercise_plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.UserExercisePlan
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExercisePlanViewModel : ViewModel() {

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
    private var exerciseResults: RealmResults<Exercise>? = null
    private var exerciseLevelResults: RealmResults<ExerciseLevel>? = null
    private var userExercisePlanId: String? = null
    private var userExercisePlan: UserExercisePlan? = null

    // Companion
    companion object {
        const val LEVELS_KEY = "levels"
        const val SELECTED_LEVEL_INDEX_KEY = "selectedLevelIndex"
        const val IS_EXERCISE_SELECTED_KEY = "isExerciseSelected"
        private const val SELECTED_EXERCISE_KEY = "selectedExercise"
        private const val SELECTED_LEVEL_KEY = "selectedLevel"
    }

    // Lifecycle
    fun inputUserExercisePlanId(userExercisePlanId: String?) {
        if (!userExercisePlanId.isNullOrEmpty()) {
            this.userExercisePlanId = userExercisePlanId
        }
        viewModelScope.launch {
            getData()
            updateAvailableInfo()
            _exerciseSequenceStateList.value = generateExerciseSequenceList()
            _exercisesStateList.value = generateExerciseList(exerciseResults, exerciseLevelResults)
            observeData()
        }
    }

    // Functions
    private suspend fun getData() {
        getExerciseResults()
        getExerciseLevelResults()
        getUserExercisePlan()
    }

    private suspend fun getExerciseResults() {
        exerciseResults = RealmManager.read(Exercise::class.java, sort = listOf(Pair(Exercise::position.name, Sort.ASCENDING)))
    }

    private suspend fun getExerciseLevelResults() {
        exerciseLevelResults = RealmManager.read(ExerciseLevel::class.java)
    }

    private suspend fun getUserExercisePlan() {
        val userExercisePlanId = userExercisePlanId ?: return
        userExercisePlan = RealmManager.read(UserExercisePlan::class.java, userExercisePlanId)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val exercises = exerciseResults ?: return
        val exerciseLevels = exerciseLevelResults ?: return

        combine(
            exercises.asFlow(),
            exerciseLevels.asFlow(),
        ) { exercisesChanges, exerciseLevelsChanges ->
            exerciseResults = exercisesChanges.list
            exerciseLevelResults = exerciseLevelsChanges.list
        }
            .debounce(500)
            .collectLatest {
                _exercisesStateList.value = generateExerciseList(exerciseResults, exerciseLevelResults)
            }
    }

    private fun updateAvailableInfo() {
        val userExercisePlan = userExercisePlan ?: return
        if (!userExercisePlan.isValid()) return

        _planeName.value = userExercisePlan.name ?: ""
        _durationState.value = userExercisePlan.duration.toInt()
        _enableAddExerciseButton.value = true
    }

    private fun generateExerciseSequenceList(): List<DynamicUIItem> {
        val defaultList = MutableList(4) {
            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.toString(),
                backgroundColor = 0
            )
        }
        val userExercisePlan = userExercisePlan ?: return defaultList
        if (!userExercisePlan.isValid()) return defaultList
        val exercises = exerciseResults ?: return defaultList
        val exerciseLevels = exerciseLevelResults ?: return defaultList
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

    @Suppress("NAME_SHADOWING")
    private fun generateExerciseList(exerciseResults: RealmResults<Exercise>?,
                                     exerciseLevelResults: RealmResults<ExerciseLevel>?): List<DynamicUIItem> {
        val exerciseLevelResults = exerciseLevelResults ?: return listOf()
        val exerciseResults = exerciseResults ?: return listOf()

        return exerciseResults.mapNotNull { exercise ->
            if (!exercise.isValid()) return@mapNotNull null

            val levelItems = exercise.levels.mapNotNull {
                val level = exerciseLevelResults.firstOrNull { level -> level.isValid() && level.id == it } ?: return@mapNotNull null

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

        userExercisePlanId?.let { id ->
            val docRef = FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_EXERCISE_PLANS, id))

            docRef.update(map)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        RealmManager.update(UserExercisePlan::class.java, id,  map)
                        completion.invoke(true)
                    }
                }
                .addOnFailureListener {
                    completion.invoke(false)
                }

        } ?: run {
            val docRef = FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_EXERCISE_PLANS, null))
            map["id"] = docRef.id
            FirebaseAuthManager.getCurrentUserCode()?.let {
                map["user_code"] = it
            }

            docRef.set(map).addOnCompleteListener {
                completion.invoke(it.isSuccessful)
            }
        }
    }
}