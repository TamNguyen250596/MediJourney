package com.example.medijourney.modules.health_center.main.sub_ui.recommend_exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.composes.VGlideImageItem

@Composable
fun RecommendedExerciseList(viewModel: RecommendedExerciseViewModel, onClick: (ImageItemModel) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    Column {
        Text(
            text = stringResource(R.string.recommended_exercises),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )
        LazyRow(
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemModels) { exercise ->
                VGlideImageItem(
                    itemModel = exercise,
                    imageModifier = Modifier.size(200.dp).padding(bottom = 8.dp),
                    titleModifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
                    onClick = onClick
                )
            }
        }
    }
}
