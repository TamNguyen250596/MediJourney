package com.example.medijourney.modules.dashboard.medication_list.sub_views

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
fun MedicalItem(itemModel: DynamicUIItem, onSelectedItem: () -> Unit) {

    // Properties
    var bitmap by rememberSaveable { mutableStateOf<Uri?>(null) }

    // LaunchedEffect
    LaunchedEffect(itemModel.image?.url) {
        itemModel.image?.url?.let {
            FirebaseStorageManager.downloadImage(it) { downloadedBitmap ->
                bitmap = downloadedBitmap
            }
        }
    }

    // Content
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelectedItem
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlideImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                model = bitmap,
                contentDescription = itemModel.image?.name
            )

            itemModel.title?.let {
                Text(
                    it.text,
                    modifier = Modifier.padding(start = 8.dp),
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            }

            itemModel.description?.let {
                Text(
                    it.text,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    color = colorResource(R.color.deep_turquoise_blue_color)
                )
            }
        }
    }
}