package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.ui_models.MTextStyle

@Composable
fun TextFieldItem(title: MTextStyle?,
                  description: MTextStyle,
                  errorMessage: String?,
                  onValueChange: (String) -> Unit) {

    // Properties
    var des by rememberSaveable { mutableStateOf(description.text) }

    // Content
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        title?.let {
            val titleText = if (title.resourceTextId != 0) {
                stringResource(id = title.resourceTextId)
            } else {
                title.text
            }

            Text(
                text = titleText,
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = description.text,
            onValueChange = {
                des = it
                onValueChange(it)
            },
            textStyle = TextStyle(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            ),
            isError = (errorMessage != null)
        )

        errorMessage?.let {
            Text(
                text = it,
                fontFamily = proximaNovaFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorResource(id = R.color.primary_red_color)
            )
        }
    }
}