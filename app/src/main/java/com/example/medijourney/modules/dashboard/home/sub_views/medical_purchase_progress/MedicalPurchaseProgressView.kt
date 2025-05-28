package com.example.medijourney.modules.dashboard.home.sub_views.medical_purchase_progress

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.DynamicUIItem

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MedicalPurchaseProgressView(viewModel: MedicalPurchaseProgressViewModel,
                                onSelectedItem: (DynamicUIItem) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.medical_purchase_progress),
            fontFamily = proximaNovaFamily,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.teal_blue).copy(alpha = 0.2f)
            ),
        ) {
            val screenWidthDp = LocalConfiguration.current.screenWidthDp
            val width = (screenWidthDp - 16.dp.value.toInt() * 2) / 4

            LazyVerticalGrid(
                modifier = Modifier.heightIn(
                    max = 100.dp
                ),
                columns = GridCells.FixedSize(width.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = false
            ) {
                items(
                    items = itemModels,
                    key = { it.itemTag }
                ) {
                    MedicalPurchaseProgressItem(
                        itemModel = it,
                        onSelectedItem = {
                            onSelectedItem(it)
                        }
                    )
                }
            }
        }
    }
}