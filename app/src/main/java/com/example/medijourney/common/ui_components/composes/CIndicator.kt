package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R

@Composable
fun CIndicator(modifier: Modifier = Modifier.size(48.dp)) {

    // Content
    CircularProgressIndicator(
        modifier = modifier,
        color = colorResource(id = R.color.ocean_blue_color),
        trackColor = colorResource(id = R.color.light_blue_color),
    )
}