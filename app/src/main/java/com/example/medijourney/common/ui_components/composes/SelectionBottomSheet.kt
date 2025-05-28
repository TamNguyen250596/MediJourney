package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.DynamicUIItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(itemModels: List<DynamicUIItem>,
                         skipPartiallyExpanded: Boolean,
                         selectedItemTag: String?,
                         onDismissRequest: () -> Unit,
                         onSelectedItem: (DynamicUIItem) -> Unit) {

    // Properties
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    // Content
    ModalBottomSheet(
        modifier = Modifier.Companion.fillMaxHeight(),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            IconButton(
                onClick = onDismissRequest
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "",
                    colorFilter = ColorFilter.Companion.tint(colorResource(id = R.color.disable_grey_color))
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(
                    items = itemModels,
                    key = { it.itemTag }
                ) { item ->

                    SearchResultItem(
                        itemModel = item,
                        isHighlight = item.itemTag == selectedItemTag,
                        onItemClicked = {
                            onSelectedItem(item)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}