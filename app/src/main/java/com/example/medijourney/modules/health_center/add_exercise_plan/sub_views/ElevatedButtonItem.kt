package com.example.medijourney.modules.health_center.add_exercise_plan.sub_views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.ui_models.MTextStyle

@Composable
fun ElevatedButtonItem(title: MTextStyle,
                       border: BorderStroke?,
                       onClick: () -> Unit) {

    // Content
    ElevatedButton(
        onClick = { onClick() },
        modifier = Modifier.padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        border = border
    ) {
        val titleText = if (title.resourceTextId != 0) {
            stringResource(id = title.resourceTextId)
        } else {
            title.text
        }

        Text(
            text = titleText,
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colorResource(id = R.color.deep_turquoise_blue_color)
        )
    }
}