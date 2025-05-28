package com.example.medijourney.modules.chat.message.sub_views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.DynamicUIItem

@Composable
fun PinnedMessageItem(modifier: Modifier, itemModel: DynamicUIItem, onClick: () -> Unit) {

    // Content
    OutlinedCard(
        modifier = modifier,
        border = BorderStroke(1.dp, colorResource(R.color.deep_turquoise_blue_color)),
        onClick = onClick
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val (textColumn, image) = createRefs()

            Column(
                modifier = Modifier.constrainAs(textColumn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(image.start, 16.dp)
                    width = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemModel.title?.let {
                    Text(
                        text = stringResource(R.string.pinned_message, it.text),
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        color = colorResource(R.color.deep_turquoise_blue_color),
                        textAlign = TextAlign.Start
                    )
                }

                itemModel.description?.let {
                    Text(
                        text = it.text,
                        maxLines = 2,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        color = colorResource(R.color.black),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Image(
                modifier = Modifier.constrainAs(image) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.value(24.dp)
                    height = Dimension.value(24.dp)
                },
                painter = painterResource(id = R.drawable.ic_pin),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
            )
        }
    }
}