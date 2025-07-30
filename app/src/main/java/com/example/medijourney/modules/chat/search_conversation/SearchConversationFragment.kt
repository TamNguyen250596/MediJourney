package com.example.medijourney.modules.chat.search_conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.LImage3TextsRButtonView
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import kotlinx.coroutines.delay

class SearchConversationFragment : Fragment() {

    // Properties
    private val viewModel: SearchConversationViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            SearchConversationScreen(viewModel) {
                IndicatorHandler.show(requireContext())
                viewModel.addUserConversation(it) {
                    IndicatorHandler.hide()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchConversationScreen(viewModel: SearchConversationViewModel, onAddClick: (DynamicUIItem) -> Unit) {

    // Properties
    val isLoading by viewModel.isLoading.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }
    val itemModels by viewModel.itemModels.collectAsState()
    val listState = rememberLazyListState()
    var showPlaceholder by remember { mutableStateOf(false) }

    // LaunchedEffect
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                lastIndex?.let {
                    viewModel.fetchNextPage(lastIndex)
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
                            viewModel.searchTextFlow.value = it
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
                    LImage3TextsRButtonView(
                        modifier = Modifier.fillMaxWidth(),
                        itemModel = item,
                        rightButtonIconId = R.drawable.ic_plus_circle,
                        onClickRightButton = {
                            onAddClick(item)
                        },
                        onClickItem = {}
                    )
                }
            }

            if (showPlaceholder) {
                EmptyPlaceholder(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.empty_conversation_placeholder)
                )
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }
}
