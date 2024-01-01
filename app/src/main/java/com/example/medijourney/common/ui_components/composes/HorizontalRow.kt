package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMostWrapContent
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.DynamicUIItem

@Composable
fun HorizontalRow(itemModel: DynamicUIItem) {

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val (title, description) = createRefs()

        itemModel.title?.let {
            Text(
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(description.start, 16.dp)
                    width = Dimension.fillToConstraints.atMostWrapContent
                },
                text = it.text,
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }

        itemModel.description?.let {
            Text(
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(title.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                text = it.text,
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                textAlign = TextAlign.End
            )
        }
    }
}