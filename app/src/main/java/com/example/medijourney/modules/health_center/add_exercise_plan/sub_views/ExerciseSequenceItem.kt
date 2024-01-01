package com.example.medijourney.modules.health_center.add_exercise_plan.sub_views

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ExerciseSequenceItem(model: DynamicUIItem,
                         onAddExercise: (DynamicUIItem) -> Unit,
                         onRemoveExercise: (DynamicUIItem) -> Unit) {

    // Content
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(8f)
                .height(96.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                model.image?.url?.let { url ->
                    var bitmap by remember { mutableStateOf<Uri?>(null) }

                    LaunchedEffect(url, model.itemTag) {
                        FirebaseStorageManager.downloadImage(url) { downloadedBitmap ->
                            bitmap = downloadedBitmap
                        }
                    }
                    GlideImage(
                        model = bitmap,
                        contentDescription = model.image?.name,
                        modifier = Modifier.size(80.dp)
                    )
                }

                model.title?.let {
                    Text(
                        text = it.text,
                        fontFamily = proximaNovaFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = TextUnit(16f, TextUnitType.Sp)
                    )
                }
            }
        }

        IconButton(
            modifier = Modifier.weight(1f),
            onClick = { onAddExercise(model) }
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_plus_circle),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.forest_green_color))
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = { onRemoveExercise(model) }
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_minus_circle),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.primary_red_color))
            )
        }
    }
}