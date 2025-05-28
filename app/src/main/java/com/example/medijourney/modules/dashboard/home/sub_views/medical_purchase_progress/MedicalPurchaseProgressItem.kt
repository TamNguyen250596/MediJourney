package com.example.medijourney.modules.dashboard.home.sub_views.medical_purchase_progress

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem

@Composable
fun MedicalPurchaseProgressItem(itemModel: DynamicUIItem, onSelectedItem: () -> Unit) {

    // Properties
    var imageResourceId by rememberSaveable { mutableStateOf<Int?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image?.name) {
        itemModel.image?.name?.let {
            imageResourceId = ImageHelper.getResourceIdByName(it)
        }
    }

    // Content
    Card(
        onClick = onSelectedItem,
        modifier = Modifier.padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.transparent)
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Box {
                imageResourceId?.let {
                    Image(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(id = it),
                        contentDescription = itemModel.image?.name
                    )
                }

                itemModel.description?.let {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = 6.dp)
                            .size(16.dp)
                            .background(
                                color = colorResource(R.color.primary_red_color),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                    ) {
                        Text(
                            text = it.text,
                            color = colorResource(R.color.white),
                            fontFamily = proximaNovaFamily,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            itemModel.title?.let {
                Text(
                    text = it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}