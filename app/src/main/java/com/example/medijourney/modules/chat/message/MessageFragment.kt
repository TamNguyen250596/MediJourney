package com.example.medijourney.modules.chat.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.MessageInputField
import com.example.medijourney.databinding.FragmentMessageBinding
import com.example.medijourney.modules.chat.message.sub_views.MessageListView
import com.example.medijourney.modules.chat.message.sub_views.PinnedMessageItem
import kotlinx.coroutines.delay

class MessageFragment : Fragment(), MenuProvider {

    // Properties
    private val viewModel: MessageViewModel by viewModels()
    private lateinit var binding: FragmentMessageBinding
    private val args: MessageFragmentArgs by navArgs()
    private val conversationId: String by lazy { args.conversationId }
    private val searchUserMessageId: String? by lazy { args.searchUserMessageId }

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MessageScreen(viewModel)
            }
        }
        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.message_search_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_search_message -> {
                openSearchMessageFragment()
            }
            else -> {
                findNavController().popBackStack()
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        viewModel.onViewCreated(conversationId)
        observeNavController()
    }

    // Functions
    private fun setupView() {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun  observeViewModel() {
        viewModel.viewTitle.observe(viewLifecycleOwner) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = it
        }
        viewModel.messageListReady.observe(viewLifecycleOwner) {
            searchUserMessageId?.let {
                viewModel.updateSelectedSearchMessageIndex(it)
            }
        }
    }

    private fun observeNavController() {
        findNavController()
            .currentBackStackEntry?.
            savedStateHandle?.
            getLiveData<String>(Constants.HIGHLIGHT_USER_MESSAGE_ID)?.
            observe(viewLifecycleOwner) { result ->
            viewModel.updateSelectedSearchMessageIndex(result)
        }
    }

    // Router
    private fun openSearchMessageFragment() {
        val action = MessageFragmentDirections.actionMessageFragmentToSearchMessageFragment()
        action.conversationId = conversationId
        findNavController().navigate(action)
    }
}

@Composable
fun MessageScreen(viewModel: MessageViewModel) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()
    val listState = rememberLazyListState()
    val pinnedItemModels by viewModel.pinnedItemModels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var currentPinnedMessageIndex by remember { mutableStateOf<Int?>(null) }
    var highlightMessageIndex by remember { mutableStateOf<Int?>(null) }
    val selectedSearchMessageIndex by viewModel.selectedSearchMessageIndex.collectAsState()

    // LaunchedEffect
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                lastIndex?.let {
                    viewModel.fetchNextPage(lastIndex)
                }
            }
    }

    LaunchedEffect(itemModels.size) {
        if (viewModel.shouldScrollToBottom) {
            listState.animateScrollToItem(0)
            viewModel.shouldScrollToBottom = false
        }
    }

    LaunchedEffect(currentPinnedMessageIndex) {
        currentPinnedMessageIndex?.let {
            val index = viewModel.getPinnedUserMessageIndex(it)
            highlightMessageIndex = index
            listState.scrollToItem(index)
            delay(1000)
            highlightMessageIndex = null
        }
    }

    LaunchedEffect(selectedSearchMessageIndex) {
        selectedSearchMessageIndex?.let {
            highlightMessageIndex = it
            listState.scrollToItem(it)
            delay(1000)
            highlightMessageIndex = null
        }
    }

    // Content
    ConstraintLayout(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
    ) {
        val (pinnedMessageList, messageList, inputFieldColumn, indicator) = createRefs()
        val hasPinnedItems = pinnedItemModels.isNotEmpty()

        MessageListView(
            modifier = Modifier.constrainAs(messageList) {
                top.linkTo(parent.top)
                bottom.linkTo(inputFieldColumn.top, 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
                width = Dimension.matchParent
            },
            listState = listState,
            highlightMessageIndex = highlightMessageIndex,
            itemModels = itemModels,
            onAction = { action, itemModel ->
                viewModel.handleAction(action, itemModel)
            }
        )

        if (hasPinnedItems) {
            PinnedMessageItem(
                modifier = Modifier.constrainAs(pinnedMessageList) {
                    top.linkTo(parent.top, 16.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)
                    height = Dimension.wrapContent
                },
                itemModel = pinnedItemModels[currentPinnedMessageIndex ?: 0],
                onClick = {
                    currentPinnedMessageIndex = viewModel.getNextPinnedMessageIndex(currentPinnedMessageIndex)
                    viewModel.didSelectedPinMsg = true
                }
            )
        }

        MessageInputField(
            modifier = Modifier.constrainAs(inputFieldColumn) {
                top.linkTo(messageList.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.wrapContent
            },
            onSend = { message, imageUri ->
                viewModel.sendMessage(message, imageUri)
            }
        )

        if (isLoading) {
            CIndicator(
                modifier = Modifier.constrainAs(indicator) {
                    centerHorizontallyTo(parent)
                    centerVerticallyTo(parent)
                    width = Dimension.value(48.dp)
                    height = Dimension.value(48.dp)
                }
            )
        }
    }
}