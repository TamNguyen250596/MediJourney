package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.models.item_models.DynamicUIItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(itemModels: List<DynamicUIItem>,
                 emptyPlaceHolderText: String,
                 highlightItemTag: String?,
                 onQueryChange: (String) -> Unit,
                 onLastIndexAppear: (Int) -> Unit,
                 onItemClicked: (DynamicUIItem) -> Unit) {

    // Properties
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var showPlaceholder by remember { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var highlightItemTag by remember { mutableStateOf<String?>(highlightItemTag) }

    // LaunchedEffect
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                lastIndex?.let {
                    onLastIndexAppear(it)
                }
            }
    }

    LaunchedEffect(itemModels) {
        if (itemModels.isEmpty()) {
            delay(1000)
            showPlaceholder = true
        } else {
            showPlaceholder = false
        }
        isLoading = false
    }

    // Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchText,
                        onQueryChange = {
                            searchText = it
                            onQueryChange(it)
                            isLoading = true
                        },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {},
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colorResource(R.color.disable_grey_color)
                            )
                        },
                        trailingIcon = {},
                    )
                },
                expanded = false,
                onExpandedChange = {},
                windowInsets = WindowInsets(top = 0),
                content = {}
            )

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = itemModels,
                    key = { it.itemTag }
                ) { item ->
                    SearchResultItem(
                        itemModel = item,
                        isHighlight = highlightItemTag == item.itemTag,
                        onItemClicked = {
                            highlightItemTag = item.itemTag
                            onItemClicked(item)
                        }
                    )
                }
            }

            if (showPlaceholder) {
                EmptyPlaceholder(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    text = emptyPlaceHolderText
                )
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }
}