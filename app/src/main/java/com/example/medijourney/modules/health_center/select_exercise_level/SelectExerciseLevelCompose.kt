package com.example.medijourney.modules.health_center.select_exercise_level

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.TitleItemModel

@Composable
fun ExerciseLevelList(viewModel: SelectExerciseLevelViewModel, onClick: (TitleItemModel) -> Unit) {

    // Properties
    val levels by viewModel.levelItemModels.collectAsState()

    // Content
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(levels) { level ->
            ExerciseLevelItem(level, onClick)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ExerciseLevelItem(exerciseLevel: TitleItemModel, onClick: (TitleItemModel) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.light_blue_color)
        ),
        border = BorderStroke(1.dp, colorResource(R.color.deep_turquoise_blue_color)),
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(exerciseLevel) }) {
        Text(
            exerciseLevel.title.text,
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(16f, TextUnitType.Sp),
            modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
        )
    }
}