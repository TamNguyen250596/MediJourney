package com.example.medijourney.modules.chat.message.sub_views

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SenderAvatar(itemModel: DynamicUIItem) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image?.url) {
        itemModel.image?.url?.let {
            FirebaseStorageManager.downloadImage(it) { downloadedBitmap ->
                bitmap = downloadedBitmap
            }
        }
    }

    // Content
    itemModel.image?.url?.let {
        bitmap?.let {
            GlideImage(
                model = bitmap,
                contentDescription = itemModel.image?.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(R.color.white), CircleShape),
                contentScale = ContentScale.Crop
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(R.color.white), CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    } ?: run {
        Spacer(Modifier.size(40.dp))
    }
}