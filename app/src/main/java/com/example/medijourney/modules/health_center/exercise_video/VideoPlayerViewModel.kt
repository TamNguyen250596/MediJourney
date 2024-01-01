package com.example.medijourney.modules.health_center.exercise_video

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.medijourney.common.models.VideoModel

class VideoPlayerViewModel : ViewModel() {

    // Properties
    val mediaItems = mutableStateListOf<MediaItem>()

    // Life cycle
    fun onCreateView(videoModels: Array<VideoModel>) {
        mediaItems.addAll(generateMediaItems(videoModels))
    }

    // Functions
    private fun generateMediaItems(videoModels: Array<VideoModel>): List<MediaItem> {
        return videoModels.mapNotNull { videoModel ->
            val tag = videoModel.videoTag ?: return@mapNotNull null

            MediaItem.Builder().apply {
                setMediaId(tag)
                setMediaMetadata(
                    MediaMetadata.Builder().apply {
                        setUri("")
                        videoModel.title?.let { setDisplayTitle(it) }
                    }.build()
                )
            }.build()
        }
    }
}

//FirebaseStorageManager.downloadVideo(path)