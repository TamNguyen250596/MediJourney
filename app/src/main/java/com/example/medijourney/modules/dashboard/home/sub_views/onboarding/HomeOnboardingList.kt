package com.example.medijourney.modules.dashboard.home.sub_views.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.composes.LTitleRButtonView
import com.example.medijourney.common.ui_components.composes.VGlideImageItem

@Composable
fun HomeOnboardingList(viewModel: HomeOnboardingViewModel,
                       onShowMore: () -> Unit,
                       onClickItem: (ImageItemModel) -> Unit) {

    // Properties
    val items by viewModel.items.collectAsState()

    // Content
    Column {
        LTitleRButtonView(
            title = stringResource(R.string.onboarding),
            buttonTitle = stringResource(R.string.show_more),
            onClick = onShowMore
        )

        if (items.isEmpty()) {
            EmptyPlaceholder(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.there_is_no_appointment)
            )
        } else {

            LazyRow(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = items,
                    key = { item -> item.itemTag }
                ) { item ->
                    VGlideImageItem(
                        itemModel = item,
                        imageModifier = Modifier.size(200.dp).padding(bottom = 8.dp),
                        titleModifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
                        onClick = {
                            onClickItem(item)
                        }
                    )
                }
            }
        }
    }
}