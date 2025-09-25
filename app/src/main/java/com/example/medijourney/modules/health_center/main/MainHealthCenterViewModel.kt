package com.example.medijourney.modules.health_center.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.FitnessTrackerActivity
import com.example.medijourney.common.models.realm_models.Recipe
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.realm_models.UserRecommendExercise
import com.example.medijourney.common.models.realm_models.UserRecommendRecipe
import com.example.medijourney.common.respositories.ExerciseLevelRepository
import com.example.medijourney.common.respositories.ExerciseRepository
import com.example.medijourney.common.respositories.FitnessTrackerActivityRepository
import com.example.medijourney.common.respositories.RecipeRepository
import com.example.medijourney.common.respositories.UserExercisePlanRepository
import com.example.medijourney.common.respositories.UserFitnessTrackerRepository
import com.example.medijourney.common.respositories.UserRecommendExerciseRepository
import com.example.medijourney.common.respositories.UserRecommendRecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MainHealthCenterViewModel @Inject constructor(
    private val userFitnessTrackerRepository: UserFitnessTrackerRepository,
    private val fitnessTrackerActivityRepository: FitnessTrackerActivityRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseLevelRepository: ExerciseLevelRepository,
    private val userExercisePlanRepository: UserExercisePlanRepository,
    private val userRecommendExerciseRepository: UserRecommendExerciseRepository,
    private val userRecommendRecipeRepository: UserRecommendRecipeRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // Life cycle
    init {
        viewModelScope.launch {
            observeFS()
        }
    }

    // Functions
    private suspend fun observeFS() = supervisorScope {
        // Fitness trackers section
        launch {
            userFitnessTrackerRepository.observeUserFitnessTrackers()
        }
        launch {
            fitnessTrackerActivityRepository.observeFitnessTrackerActivities()
        }

        // Exercise plans section
        launch {
            exerciseRepository.observeExercises()
        }
        launch {
            exerciseLevelRepository.observeExerciseLevels()
        }
        launch {
            userExercisePlanRepository.observeUserExercisePlans()
        }

        // Recommended exercises section
        launch {
            userRecommendExerciseRepository.observeUserRecommendExercises()
        }

        // Recommended receipts section
        launch {
            userRecommendRecipeRepository.observeUserRecommendRecipes()
        }
        launch {
            recipeRepository.observeRecipes()
        }
    }
}