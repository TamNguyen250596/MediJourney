package com.example.medijourney.modules.dashboard.medical_product_details.sub_views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily

@Composable
fun ItemNumberView(onCountChanged: (Int) -> Unit) {

    // Properties
    var count by remember { mutableStateOf(0) }

    // Content
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = {
                count -= 1
                if (count < 0) count = 0
                onCountChanged(count)
            }
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_minus_circle),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.primary_red_color))
            )
        }

        Text(
            text = count.toString(),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )

        IconButton(
            onClick = {
                count += 1
                onCountChanged(count)
            }
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_plus_circle),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.forest_green_color))
            )
        }
    }
}