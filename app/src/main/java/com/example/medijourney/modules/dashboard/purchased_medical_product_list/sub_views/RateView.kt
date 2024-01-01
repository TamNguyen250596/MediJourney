package com.example.medijourney.modules.dashboard.purchased_medical_product_list.sub_views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R

@Composable
fun RateView(enable: Boolean, rateMax: Int, rate: Int, onRate: (Int) -> Unit) {

    // Properties
    var currentRate by rememberSaveable { mutableIntStateOf(rate) }

    // Content
    LazyRow {
        items(rateMax) { index ->
            val isRated = index + 1 <= currentRate

            IconButton(
                enabled = enable,
                onClick = {
                    currentRate = index + 1
                    onRate(currentRate)
                },
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = if (isRated) R.drawable.ic_fill_star else R.drawable.ic_star),
                    contentDescription = ""
                )
            }
        }
    }
}