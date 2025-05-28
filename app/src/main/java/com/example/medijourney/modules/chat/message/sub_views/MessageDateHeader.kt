package com.example.medijourney.modules.chat.message.sub_views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.R

@Composable
fun MessageDateHeader(date: String) {

    // Content
    Text(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        text = date,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(12f, TextUnitType.Sp),
        color = colorResource(R.color.black),
        textAlign = TextAlign.Center
    )
}