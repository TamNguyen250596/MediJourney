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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ConversationItem(modifier: Modifier,
                     itemModel: DynamicUIItem,
                     showAddButton: Boolean,
                     onAddClick: (DynamicUIItem) -> Unit,
                     onSelect: (DynamicUIItem) -> Unit) {

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
        onClick = {
            onSelect(itemModel)
        }
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
                    if (showAddButton) {
                        end.linkTo(iconButton.start, 16.dp)
                    } else {
                        end.linkTo(parent.end, 8.dp)
                    }
                    width = Dimension.fillToConstraints
                },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bitmap != null) {
                    GlideImage(
                        model = bitmap,
                        contentDescription = itemModel.image?.name,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                } else if (imageResourceId != null) {
                    Image(
                        painter = painterResource(id = imageResourceId!!),
                        contentDescription = itemModel.image?.name,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
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

                    itemModel.secondaryDescription?.let {
                        Text(
                            text = it.text,
                            fontFamily = proximaNovaFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (showAddButton) {
                IconButton(
                    modifier = Modifier.constrainAs(iconButton) {
                        top.linkTo(parent.top, 8.dp)
                        bottom.linkTo(parent.bottom, 8.dp)
                        start.linkTo(row.end)
                        end.linkTo(parent.end, 8.dp)
                        width = Dimension.value(24.dp)
                    },
                    enabled = showAddButton,
                    onClick = { onAddClick(itemModel) }
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.ic_plus_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.forest_green_color))
                    )
                }
            }
        }
    }
}