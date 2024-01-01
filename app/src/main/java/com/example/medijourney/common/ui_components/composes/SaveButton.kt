package com.example.medijourney.common.ui_components.composes

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily

@Composable
fun SaveButton(modifier: Modifier = Modifier,
               enableSaveButton: Boolean,
               onSaveClick: () -> Unit) {

    // Content
    Button(
        onClick = { onSaveClick() },
        modifier = modifier,
        enabled = enableSaveButton,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enableSaveButton)
                colorResource(id = R.color.deep_turquoise_blue_color)
            else colorResource(id = R.color.disable_grey_color)
        )
    ) {
        Text(
            text = stringResource(R.string.save),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Center
        )
    }
}