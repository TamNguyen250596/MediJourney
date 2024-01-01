package com.example.medijourney.common.ui_components.composes

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.ImageItemModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VGlideImageItem(itemModel: ImageItemModel,
                    modifier: Modifier = Modifier,
                    cardShape: Shape = CardDefaults.shape,
                    imageModifier: Modifier = Modifier,
                    imageContentMode: ContentScale = ContentScale.Crop,
                    titleModifier: Modifier = Modifier,
                    onClick: (ImageItemModel) -> Unit) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image.url) {
        itemModel.image.url?.let {
            FirebaseStorageManager.downloadImage(it) { downloadedBitmap ->
                bitmap = downloadedBitmap
            }
        }
    }

    // Content
    Card(
        modifier = modifier,
        shape = cardShape,
        onClick = { onClick(itemModel) }
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlideImage(
                model = bitmap,
                contentDescription = itemModel.image.name,
                modifier = imageModifier,
                contentScale = imageContentMode
            )

            itemModel.title?.let {
                Text(
                    it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    modifier = titleModifier,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}