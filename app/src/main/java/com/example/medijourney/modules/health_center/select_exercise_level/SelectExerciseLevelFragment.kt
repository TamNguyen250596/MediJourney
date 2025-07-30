package com.example.medijourney.modules.health_center.select_exercise_level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectExerciseLevelFragment : BottomSheetDialogFragment() {

    // Properties
    private val viewModel: SelectExerciseLevelViewModel by viewModels()
    private val args : SelectExerciseLevelFragmentArgs by navArgs()
    private val exerciseId: Int by lazy { args.exerciseId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            ExerciseLevelList(viewModel) {
                openVideoPlayer(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(exerciseId)
    }

    // Router
    private fun openVideoPlayer(item: TitleItemModel) {
        val videoModels = viewModel.getVideoModels(item)
        val action = SelectExerciseLevelFragmentDirections.actionSelectExerciseLevelFragmentToVideoPlayerFragment(videoModels)
        findNavController().navigate(action)
    }
}