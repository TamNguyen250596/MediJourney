package com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.composes.LImage3TextsRButtonView
import com.example.medijourney.common.ui_components.composes.LTitleRButtonView

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FitnessTrackerView(
    viewModel: FitnessTrackersViewModel,
    onClickAdd: () -> Unit,
    onClickItem: (DynamicUIItem) -> Unit
) {

    // Properties
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val width = if (viewModel.itemModels.size > 1) {
            (screenWidthDp - 32.dp.value.toInt()) * 3 / 4
    } else {
        screenWidthDp - 32.dp.value.toInt()
    }

    // Content
    Column {
        LTitleRButtonView(
            title = stringResource(R.string.fitness_trackers),
            buttonTitle = stringResource(R.string.add),
            onClick = onClickAdd
        )

        if (viewModel.itemModels.isEmpty()) {
            EmptyPlaceholder(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.empty_fitness_tracker_description)
            )
        } else {
            LazyRow(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.itemModels,
                    key = { item -> item.itemTag }
                ) { item ->
                    LImage3TextsRButtonView(
                        modifier = Modifier.width(width.dp),
                        itemModel = item,
                        rightButtonIconId = R.drawable.ic_right_arrow,
                        rightButtonTintColorId = R.color.disable_grey_color,
                        onClickRightButton = {},
                        onClickItem = {
                            onClickItem(item)
                        }
                    )
                }
            }
        }
    }
}