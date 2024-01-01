package com.example.medijourney.modules.health_center.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.models.VideoModel
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.Recipe
import com.example.medijourney.databinding.FragmentHealthCenterBinding
import com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list.ExercisePlanList
import com.example.medijourney.modules.health_center.main.sub_ui.exercise_plan_list.ExercisePlanListViewModel
import com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.FitnessTrackersFragment
import com.example.medijourney.modules.health_center.main.sub_ui.healthy_receipts.RecommendedRecipeList
import com.example.medijourney.modules.health_center.main.sub_ui.healthy_receipts.RecommendedRecipesViewModel
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseList
import com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises.RecommendedExerciseViewModel
import io.realm.kotlin.ext.isValid


class MainHealthCenterFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentHealthCenterBinding
    private val viewModel: MainHealthCenterViewModel by viewModels()
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
    ): View {
        binding = FragmentHealthCenterBinding.inflate(inflater, container, false)
        setupComposes()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        exercisePlanListViewModel.onViewCreated()
        recommendedExerciseViewModel.onViewCreated()
        recommendedRecipesViewModel.onViewCreated()
    }

    // Functions
    private fun setupComposes() {
        binding.excPlanComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
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
            }
        }
        binding.recommendExcComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RecommendedExerciseList(recommendedExerciseViewModel, onClick = {
                    openSelectExerciseLevelFragment(it)
                })
            }
        }
        binding.recommendReceiptComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RecommendedRecipeList(recommendedRecipesViewModel, onClick = {
                    openRecipeVideoFragment(it)
                })
            }
        }
    }

    private fun setupView() {
        val fitnessTrackersFragmentTag = FitnessTrackersFragment::class.simpleName
        val fitnessTrackersFragment = childFragmentManager.findFragmentByTag(fitnessTrackersFragmentTag) as? FitnessTrackersFragment
            ?: FitnessTrackersFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.fitnessTrackersContainerView, fitnessTrackersFragment, fitnessTrackersFragmentTag)
            .commit()
    }

    // Routers
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
