package com.example.medijourney.modules.chat.search_message

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import com.example.medijourney.common.ui_components.composes.SearchMessageItem
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentSearchMessageBinding
import kotlinx.coroutines.delay

class SearchMessageFragment : Fragment() {

    // Properties
    private val viewModel: SearchMessageViewModel by viewModels()
    private lateinit var binding: FragmentSearchMessageBinding
    private val args: SearchMessageFragmentArgs by navArgs()
    private val conversationId: String? by lazy { args.conversationId }

    // View cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchMessageBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SearchMessageScreen(viewModel = viewModel) {
                    goBackThePreviousFragment(it)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(conversationId)
    }

    // View cycle
    private fun goBackThePreviousFragment(itemModel: DynamicUIItem) {
        IndicatorHandler.show(requireContext())
        viewModel.handleSearchMessageClick(itemModel) {
            IndicatorHandler.hide()
            findNavController().apply {
                previousBackStackEntry?.savedStateHandle?.set(Constants.HIGHLIGHT_USER_MESSAGE_ID, it)
                popBackStack()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMessageScreen(viewModel: SearchMessageViewModel, onSelect: (DynamicUIItem) -> Unit) {

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
                    viewModel.fetchNextPage(it)
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
                    SearchMessageItem(itemModel = item) {
                        onSelect(item)
                    }
                }
            }

            if (showPlaceholder) {
                EmptyPlaceholder(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    text = stringResource(R.string.empty_message_placeholder)
                )
            }
        }

        if (isLoading) {
            CIndicator()
        }
    }
}
