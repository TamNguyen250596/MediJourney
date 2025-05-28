package com.example.medijourney.modules.dashboard.add_medical_booking.sub_views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem

@Composable
fun AddMedicalBookingItem(itemModel: DynamicUIItem, onClickItem: () -> Unit) {

    // Properties
    var imageResourceId by rememberSaveable { mutableStateOf<Int?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.secondaryImage?.name) {
        itemModel.secondaryImage?.name?.let {
            imageResourceId = ImageHelper.getResourceIdByName(it)
        }
    }

    // Content
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val enable = itemModel.getAdditionalValue(Constants.ENABLE_FIELD) as? Boolean == true
        itemModel.title?.text?.let {
            Text(
                text = it,
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (enable) colorResource(id = R.color.deep_turquoise_blue_color)
                else colorResource(id = R.color.deep_turquoise_blue_color).copy(0.7f)
            )
        }

        Card (
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(if (enable) 1f else 0.7f),
            onClick = onClickItem,
            enabled = enable
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val (textRef, imageRef) = createRefs()

                itemModel.description?.text?.let {
                    Text(
                        text = it,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.black),
                        modifier = Modifier.constrainAs(textRef) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                    )
                }

                imageResourceId?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = itemModel.image?.name,
                        modifier = Modifier.constrainAs(imageRef) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.wrapContent
                        }
                    )
                }
            }
        }
    }
}