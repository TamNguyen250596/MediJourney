package com.example.medijourney.modules.health_center.exercise_video

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.fragment.navArgs
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.VideoModel
import com.example.medijourney.common.ui_components.composes.VideoPlayer

class VideoPlayerFragment : Fragment() {

    // Properties
    private val viewModel: VideoPlayerViewModel by viewModels()
    private val args : VideoPlayerFragmentArgs by navArgs()
    private val videoModels: Array<VideoModel> by lazy { args.videoModels }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            VideoPlayer(
                mediaItems = viewModel.mediaItems,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onCreateView(videoModels)
    }
}