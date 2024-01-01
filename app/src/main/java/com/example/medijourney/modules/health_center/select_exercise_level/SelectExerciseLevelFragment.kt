package com.example.medijourney.modules.health_center.select_exercise_level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.databinding.FragmentSelectExerciseLevelBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectExerciseLevelFragment : BottomSheetDialogFragment() {

    // Properties
    private val viewModel: SelectExerciseLevelViewModel by viewModels()
    private lateinit var binding: FragmentSelectExerciseLevelBinding
    private val args : SelectExerciseLevelFragmentArgs by navArgs()
    private val exerciseId: Int by lazy { args.exerciseId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectExerciseLevelBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ExerciseLevelList(viewModel) {
                    openVideoPlayer(it)
                }
            }
        }
        return binding.root
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