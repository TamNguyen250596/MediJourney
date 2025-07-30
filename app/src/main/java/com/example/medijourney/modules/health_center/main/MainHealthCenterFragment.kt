package com.example.medijourney.modules.health_center.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.VideoModel
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.Recipe
import com.example.medijourney.modules.health_center.add_fitness_tracker.AddFitnessTrackerFragment
import com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list.ExercisePlanList
import com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list.ExercisePlanListViewModel
import com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.FitnessTrackerView
import com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.FitnessTrackersViewModel
import com.example.medijourney.modules.health_center.main.sub_ui.healthy_receipts.RecommendedRecipeList
import com.example.medijourney.modules.health_center.main.sub_ui.healthy_receipts.RecommendedRecipesViewModel
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseList
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseViewModel
import io.realm.kotlin.ext.isValid


class MainHealthCenterFragment : Fragment() {

    // Properties
    private val viewModel: MainHealthCenterViewModel by viewModels()
    private val fitnessTrackersViewModel: FitnessTrackersViewModel by viewModels()
    private val exercisePlanListViewModel: ExercisePlanListViewModel by viewModels()
    private val recommendedExerciseViewModel: RecommendedExerciseViewModel by viewModels()
    private val recommendedRecipesViewModel: RecommendedRecipesViewModel by viewModels()

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainHealthCenterSubViews()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exercisePlanListViewModel.onViewCreated()
        recommendedExerciseViewModel.onViewCreated()
        recommendedRecipesViewModel.onViewCreated()
    }

    // Functions
    @Composable
    private fun MainHealthCenterSubViews() {
        FitnessTrackerView(
            viewModel = fitnessTrackersViewModel,
            onClickAdd = {
                openAddFitnessTrackerFragment()
            },
            onClickItem = {
                openFitnessTrackerDetails(it)
            }
        )

        ExercisePlanList(
            viewModel = exercisePlanListViewModel,
            onAddNewPlan = {
                openAddExercisePlanFragment()
            },
            onPlanSelected = {
                val videoModels = exercisePlanListViewModel.getVideoModels(it)
                openVideoPlayerFragment(videoModels)
            },
            onShowPlanInfo = {
                val userExercisePlanId = exercisePlanListViewModel.getUserExercisePlanId(it)
                openUserExercisePlanDetail(userExercisePlanId)
            }
        )

        RecommendedExerciseList(
            viewModel = recommendedExerciseViewModel,
            onClick = {
            openSelectExerciseLevelFragment(it)
            }
        )

        RecommendedRecipeList(
            viewModel = recommendedRecipesViewModel,
            onClick = {
            openRecipeVideoFragment(it)
            }
        )
    }

    // Routers
    private fun openAddFitnessTrackerFragment() {
        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToAddFitnessTrackerFragment(null)
        action.viewType = AddFitnessTrackerFragment.ADD_FITNESS_TRACKER
        findNavController().navigate(action)
    }

    private fun openFitnessTrackerDetails(model: DynamicUIItem) {
        val id = fitnessTrackersViewModel.getUserFitnessTrackerId(model) ?: return
        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToFitnessTrackerDetailFragment(id, null)
        findNavController().navigate(action)
    }

    private fun openAddExercisePlanFragment() {
        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToAddExercisePlanFragment()
        findNavController().navigate(action)
    }

    private fun openUserExercisePlanDetail(userExercisePlanId: String?) {
        if (userExercisePlanId == null) return

        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToExercisePlanDetailFragment()
        action.userExercisePlanId = userExercisePlanId
        findNavController().navigate(action)
    }

    private fun openVideoPlayerFragment(videoModels: Array<VideoModel>) {
        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToVideoPlayerFragment(videoModels)
        findNavController().navigate(action)
    }

    private fun openSelectExerciseLevelFragment(itemModel: ImageItemModel) {
        val exercise = itemModel.data as? Exercise ?: return
        if (!exercise.isValid()) return

        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToSelectExerciseLevelFragment()
        action.exerciseId = exercise.id
        action.showAppBar = false
        findNavController().navigate(action)
    }

    private fun openRecipeVideoFragment(itemModel: ImageItemModel) {
        val recipe = itemModel.data as? Recipe ?: return
        if (!recipe.isValid()) return

        val action = MainHealthCenterFragmentDirections.actionHealthCenterFragmentToRecipeVideoFragment()
        action.recipeId = recipe.id
        findNavController().navigate(action)
    }
}
