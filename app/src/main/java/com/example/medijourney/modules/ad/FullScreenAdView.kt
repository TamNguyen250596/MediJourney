package com.example.medijourney.modules.ad

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.ui_models.MFont
import com.example.medijourney.common.ui_components.composes.MText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FullScreenAdView(imageUrlString: String,
                     countDownSecond: Int,
                     onClickImage: () -> Unit,
                     onClickSkip: () -> Unit) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentCountDownSecond by rememberSaveable { mutableIntStateOf(countDownSecond) }

    // LaunchEffect
    LaunchedEffect(imageUrlString) {
        FirebaseStorageManager.downloadImage(imageUrlString) { downloadedBitmap ->
            bitmap = downloadedBitmap
        }
    }

    LaunchedEffect(Unit) {
        repeat(countDownSecond) {
            delay(1.seconds)
            currentCountDownSecond--
        }
    }

    // Content
    Box(modifier = Modifier.fillMaxSize()) {
        GlideImage(
            modifier = Modifier.fillMaxSize().clickable {
                onClickImage()
            },
            model = bitmap,
            contentDescription = "",
            contentScale = ContentScale.FillBounds
        )

        Button(
            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp).align(Alignment.BottomEnd),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.white),
            ),
            border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
            onClick = {
                if (currentCountDownSecond == 0) {
                    onClickSkip()
                }
            }
        ) {
            MText(
                if (currentCountDownSecond > 0) "$currentCountDownSecond" else "Skip",
                font = MFont.bold(24f),
                textAlign = TextAlign.Center
            )
        }
    }
}