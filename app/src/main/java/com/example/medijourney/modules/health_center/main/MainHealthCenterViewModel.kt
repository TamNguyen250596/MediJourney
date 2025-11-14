package com.example.medijourney.modules.health_center.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.respositories.ExerciseLevelRepo
import com.example.medijourney.common.respositories.ExerciseRepo
import com.example.medijourney.common.respositories.FitnessTrackerActivityRepo
import com.example.medijourney.common.respositories.RecipeRepo
import com.example.medijourney.common.respositories.UserExercisePlanRepo
import com.example.medijourney.common.respositories.UserFitnessTrackerRepo
import com.example.medijourney.common.respositories.UserRecommendExerciseRepo
import com.example.medijourney.common.respositories.UserRecommendRecipeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MainHealthCenterViewModel @Inject constructor(
    private val userFitnessTrackerRepository: UserFitnessTrackerRepo,
    private val fitnessTrackerActivityRepository: FitnessTrackerActivityRepo,
    private val exerciseRepository: ExerciseRepo,
    private val exerciseLevelRepository: ExerciseLevelRepo,
    private val userExercisePlanRepository: UserExercisePlanRepo,
    private val userRecommendExerciseRepository: UserRecommendExerciseRepo,
    private val userRecommendRecipeRepository: UserRecommendRecipeRepo,
    private val recipeRepository: RecipeRepo
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
            userFitnessTrackerRepository.listenUserFitnessTrackers()
        }
        launch {
            fitnessTrackerActivityRepository.listenFitnessTrackerActivities()
        }

        // Exercise plans section
        launch {
            exerciseRepository.listenExercises()
        }
        launch {
            exerciseLevelRepository.listenExerciseLevels()
        }
        launch {
            userExercisePlanRepository.listenUserExercisePlans()
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