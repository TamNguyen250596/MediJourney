package com.example.medijourney.common.ui_components.composes

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchResultItem(itemModel: DynamicUIItem,
                     isHighlight: Boolean,
                     onItemClicked: () -> Unit) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageResourceId by rememberSaveable { mutableStateOf<Int?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image?.url) {
        itemModel.image?.url?.let {
            FirebaseStorageManager.downloadImage(it) { downloadedBitmap ->
                bitmap = downloadedBitmap
            }
        }
    }

    LaunchedEffect(itemModel.image?.name) {
        itemModel.image?.name?.let {
            imageResourceId = ImageHelper.getResourceIdByName(it)
        }
    }

    // Content
    Card(
        border = BorderStroke(1.dp, color = colorResource(if (isHighlight) R.color.forest_green_color else R.color.white)),
        onClick = onItemClicked
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when(true) {
                (bitmap != null) -> {
                    GlideImage(
                        model = bitmap,
                        contentDescription = itemModel.image?.name,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                (imageResourceId != null) -> {
                    imageResourceId?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = itemModel.image?.name,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                else -> {}
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemModel.title?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                itemModel.description?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}