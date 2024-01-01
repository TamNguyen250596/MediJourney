package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.ImageItemModel

@Composable
fun VImageItem(itemModel: ImageItemModel,
               modifier: Modifier,
               cardColors: CardColors = CardDefaults.cardColors(),
               imageModifier: Modifier = Modifier,
               imageContentMode: ContentScale = ContentScale.Crop,
               titleModifier: Modifier = Modifier,
               onClick: (ImageItemModel) -> Unit) {

    // Properties
    var imageResourceId by rememberSaveable { mutableStateOf<Int?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image.name) {
        itemModel.image.name?.let {
            imageResourceId = ImageHelper.getResourceIdByName(it)
        }
    }

    // Content
    Card(
        modifier = modifier,
        colors = cardColors,
        onClick = { onClick(itemModel) }
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageResourceId?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = itemModel.image.name,
                    contentScale = imageContentMode,
                    modifier = imageModifier
                )
            }

            itemModel.title?.let {
                Text(
                    it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(14f, TextUnitType.Sp),
                    modifier = titleModifier,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}