package com.example.medijourney.common.ui_components.composes

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(mediaItems: MutableList<MediaItem>, modifier: Modifier = Modifier) {

    // Properties
    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    val context = LocalContext.current
    var currentMediaItemIndex by remember { mutableIntStateOf(-1) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener{
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        isLoading = false
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    isLoading = false
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    isLoading = true
                    title = mediaItem?.mediaMetadata?.displayTitle?.takeIf { it.isNotBlank() }?.toString() ?: ""
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                    currentMediaItemIndex = newPosition.mediaItemIndex
                }
            })
        }
    }

    LaunchedEffect(mediaItems) {
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItems(mediaItems)

        currentMediaItemIndex = if (mediaItems.isEmpty()) {
            -1
        } else {
            0
        }
    }

    LaunchedEffect(currentMediaItemIndex) {
        if (currentMediaItemIndex < 0) return@LaunchedEffect
        val currentMediaItem = mediaItems[currentMediaItemIndex]

        Log.d("VideoPlayer.LaunchedEffect", "Loading video: ${currentMediaItem.mediaId}")
        FirebaseStorageManager.downloadVideo(currentMediaItem.mediaId) { uri ->
            uri ?: return@downloadVideo
            val updatedItem = currentMediaItem.buildUpon().setUri(uri).build()
            exoPlayer.replaceMediaItem(exoPlayer.currentMediaItemIndex, updatedItem)
            exoPlayer.prepare()
        }
    }

    // Content
    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    setBackgroundColor(ResourcesCompat.getColor(resources, R.color.black, null))
                }
            },
            update = { it.player = exoPlayer }
        )

        Text(
            text = title,
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = colorResource(id = R.color.white),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )

        if (isLoading) {
            CIndicator()
        }
    }

    // Ensure cleanup
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
}