package com.example.medijourney.modules.dashboard.home.sub_views.top_dashboard_menus

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.ui_models.MFont
import com.example.medijourney.common.ui_components.composes.MText
import com.example.medijourney.common.ui_components.composes.VImageItem

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TopDashboardMenus(viewModel: TopDashboardMenusViewModel,
                      onClickShowMore: () -> Unit,
                      onClickItem: (ImageItemModel) -> Unit) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()

    // Content
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.soft_beige).copy(alpha = 0.7f)
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 12.dp)
        ) {
            val screenWidthDp = LocalConfiguration.current.screenWidthDp
            val width = (screenWidthDp - 20.dp.value.toInt() * 2) / 3

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
                    VImageItem(
                        itemModel = it,
                        modifier = Modifier.fillMaxSize(),
                        imageModifier = Modifier.size(48.dp),
                        cardColors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.transparent)
                        ),
                        onClick = onClickItem
                    )
                }
            }

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClickShowMore
            ) {
                MText(
                    text = stringResource(R.string.onboarding),
                    font = MFont.bold(12f),
                    colorId = R.color.deep_turquoise_blue_color
                )
            }
        }
    }
}