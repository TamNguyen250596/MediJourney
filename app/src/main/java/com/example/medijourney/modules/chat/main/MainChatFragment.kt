package com.example.medijourney.modules.chat.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.LImage3TextsRButtonView
import com.example.medijourney.common.ui_components.composes.EmptyPlaceholder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainChatFragment : Fragment(), MenuProvider {

    // Properties
    private val viewModel: MainChatViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            UserConversationScreen(viewModel) {
                openMessageFragment(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeNavController()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_message_search_ai_chat_action_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_add -> openSearchConversationFragment()
            R.id.action_search_message -> openSearchMessageFragment()
            R.id.action_ai_chat -> openAIChatFragment()
        }
        return true
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun observeNavController() {
        findNavController()
            .currentBackStackEntry?.
            savedStateHandle?.
            getLiveData<String>(Constants.HIGHLIGHT_USER_MESSAGE_ID)?.
            observe(viewLifecycleOwner) { result ->
                openMessageFragment(result)
            }
    }

    // Routers
    private fun openSearchConversationFragment() {
        findNavController().navigate(R.id.action_mainChatFragment_to_searchConversationFragment)
    }

    private fun openSearchMessageFragment() {
        val action = MainChatFragmentDirections.actionMainChatFragmentToSearchMessageFragment()
        findNavController().navigate(action)
    }

    private fun openMessageFragment(item: DynamicUIItem) {
        val action = MainChatFragmentDirections.actionMainChatFragmentToMessageFragment()
        action.conversationId = viewModel.getConversationId(item)
        findNavController().navigate(action)
    }

    private fun openMessageFragment(highlightUserMessageId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val conversationId = viewModel.getConversationId(highlightUserMessageId) ?: return@launch
            val action = MainChatFragmentDirections.actionMainChatFragmentToMessageFragment()
            action.conversationId = conversationId
            action.searchUserMessageId = highlightUserMessageId
            findNavController().apply {
                currentBackStackEntry?.savedStateHandle?.remove<String>(Constants.HIGHLIGHT_USER_MESSAGE_ID)
                navigate(action)
            }
        }
    }

    private fun openAIChatFragment() {
        findNavController().navigate(R.id.action_mainChatFragment_to_aiChatFragment)
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun UserConversationScreen(viewModel: MainChatViewModel, onSelect: (DynamicUIItem) -> Unit) {

    // Properties
    val isLoading by viewModel.isLoading.collectAsState()
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
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(itemModels, key = { it.itemTag }
                ) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { newValue ->
                            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteUserConversation(item)
                                true
                            } else {
                                false
                            }
                        },
                        positionalThreshold = { distance ->
                            distance * 0.25f
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier,
                        backgroundContent = {
                            val color by
                            animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> colorResource(R.color.primary_red_color)
                                    else -> colorResource(R.color.primary_red_color).copy(alpha = 0.5f)
                                }
                            )
                            val scale by
                            animateFloatAsState(if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f)

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = color
                                ),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_delete),
                                        contentDescription = "Localized description",
                                        modifier = Modifier.scale(scale)
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        content = {
                            LImage3TextsRButtonView(
                                modifier = Modifier.fillMaxWidth(),
                                itemModel = item,
                                rightButtonIconId = R.drawable.ic_plus_circle,
                                onClickRightButton = {},
                                onClickItem = {
                                    onSelect(item)
                                }
                            )
                        }
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