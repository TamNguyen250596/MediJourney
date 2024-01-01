package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily

@Composable
fun EmptyPlaceholder(modifier: Modifier, text: String) {

    // Content
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.deep_turquoise_blue_color)
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = colorResource(R.color.white),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}