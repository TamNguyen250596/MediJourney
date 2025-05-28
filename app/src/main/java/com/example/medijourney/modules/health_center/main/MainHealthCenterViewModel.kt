package com.example.medijourney.modules.health_center.main

import androidx.lifecycle.ViewModel
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


class MainHealthCenterViewModel: ViewModel() {

    // Life cycle
    fun onCreate() {
        observeFS()
    }

    override fun onCleared() {
        super.onCleared()
        FireStoreManager.removeListeners(this::class.java)
    }

    // Functions
    private fun observeFS() {
        // Fitness trackers section
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_FITNESS_TRACKERS)
            .observe(UserFitnessTracker::class.java, this::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.FITNESS_TRACKER_ACTIVITIES)
            .whereEqualTo("enable", true)
            .observe(FitnessTrackerActivity::class.java, this::class.java)

        // Exercise plans section
        FireStoreManager.buildCollectionRef(FireStoreCollection.EXERCISES)
            .whereEqualTo("enable", true)
            .observe(Exercise::class.java, this::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.EXERCISE_LEVELS)
            .whereEqualTo("enable", true)
            .observe(ExerciseLevel::class.java, this::class.java)
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_EXERCISE_PLANS)
            .observe(UserExercisePlan::class.java, this::class.java)

        // Recommended exercises section
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_RECOMMEND_EXERCISE)
            .observe(UserRecommendExercise::class.java, this::class.java)

        // Recommended receipts section
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_RECOMMEND_RECIPES)
            .observe(UserRecommendRecipe::class.java, this::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.RECIPES)
            .whereEqualTo("enable", true)
            .observe(Recipe::class.java, this::class.java)
    }
}