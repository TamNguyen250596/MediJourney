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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.ui_models.MFont

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LImage3TextsRButtonView(
    modifier: Modifier,
    imageModifier: Modifier = Modifier.size(48.dp),
    itemModel: DynamicUIItem,
    rightButtonIconId: Int? = null,
    rightButtonTintColorId: Int = R.color.forest_green_color,
    onClickRightButton: (() -> Unit),
    onClickItem: () -> Unit)
{

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
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white),
        ),
        border = BorderStroke(1.dp, color = colorResource(R.color.deep_turquoise_blue_color)),
        onClick = onClickItem
    ) {
        ConstraintLayout(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {

            val (row, iconButton) = createRefs()

            Row(
                modifier = Modifier.constrainAs(row) {
                    top.linkTo(parent.top, 8.dp)
                    bottom.linkTo(parent.bottom, 8.dp)
                    start.linkTo(parent.start, 8.dp)
                    if (rightButtonIconId != null) {
                        end.linkTo(iconButton.start, 16.dp)
                    } else {
                        end.linkTo(parent.end, 8.dp)
                    }
                    width = Dimension.fillToConstraints
                },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    bitmap != null -> {
                        GlideImage(
                            model = bitmap,
                            contentDescription = itemModel.image?.name,
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop
                        )
                    }
                    imageResourceId != null -> {
                        val id = imageResourceId ?: return@Row

                        Image(
                            painter = painterResource(id = id),
                            contentDescription = itemModel.image?.name,
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop
                        )
                    }
                }


                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemModel.title?.let {
                        MText(
                            text = it.text,
                            font = MFont.bold(16f)
                        )
                    }

                    itemModel.description?.let {
                        MText(
                            text = it.text,
                            font = MFont.regular(16f)
                        )
                    }

                    itemModel.secondaryDescription?.let {
                        MText(
                            text = it.text,
                            font = MFont.regular(12f)
                        )
                    }
                }
            }

            rightButtonIconId?.let {
                IconButton(
                    modifier = Modifier.constrainAs(iconButton) {
                        top.linkTo(parent.top, 8.dp)
                        bottom.linkTo(parent.bottom, 8.dp)
                        start.linkTo(row.end)
                        end.linkTo(parent.end, 8.dp)
                        width = Dimension.value(24.dp)
                    },
                    onClick = onClickRightButton
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(it),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorResource(id = rightButtonTintColorId))
                    )
                }
            }
        }
    }
}