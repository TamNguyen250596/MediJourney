package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.ui_models.MFont

@Composable
fun LTitleRButtonView(title: String, buttonTitle: String, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(id = R.color.light_blue_color)
            ),
            border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MText(
                    text = buttonTitle,
                    font = MFont.regular(16f),
                    colorId = R.color.deep_turquoise_blue_color
                )
            }
        }
    }
}