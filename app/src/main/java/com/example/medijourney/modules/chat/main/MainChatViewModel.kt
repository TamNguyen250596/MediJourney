package com.example.medijourney.modules.chat.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.example.medijourney.common.respositories.MessageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainChatViewModel @Inject constructor(
    private val conversationRepo: ConversationRepo,
    private val messageRepo: MessageRepo
) : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var conversationsFlow = conversationRepo.getConversationsFlow(true, null)
    private var cursorTag: String? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            launch {
                observeFS()
            }
            launch {
                observeData()
            }
        }
    }

    // Functions
    private suspend fun observeFS() {
        conversationRepo
            .listenConversations(true, null, null)
            .collect {
                updateCursorTag(it)
            }
    }

    private suspend fun observeData() {
        conversationsFlow
            .firstThenDebounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicUIItemModels(it)
            }
    }

    private fun generateDynamicUIItemModels(
        conversations: List<Conversation>,
    ): List<DynamicUIItem> {
        return conversations.mapNotNull {
            if (!it.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
            ).apply {
                it.imageName?.let { imageName ->
                    image = ImageStyle(url = "images/conversations/${imageName}.jpg", name = "ic_group")
                } ?: run {
                    image = ImageStyle(name = "ic_group")
                }
                title = MTextStyle(it.name)
                it.shortTag?.let { shortTag ->
                    description = MTextStyle(shortTag)
                }
                it.lastMessage?.let { lastMessage ->
                    description = MTextStyle(lastMessage)
                }
            }
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        if (index >= _itemModels.value.size) return
        val item = _itemModels.value[index]
        val conversation = item.data as? Conversation ?: return
        if (!conversation.isValid()) return
        val cursorTag = cursorTag ?: return
        if (conversation.tag < cursorTag) return

        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        observeCurrentPageJob = viewModelScope.launch {
            conversationRepo
                .listenConversations(true, null, cursorTag)
                .collect {
                    updateCursorTag(it)
                }
        }
    }

    private fun updateCursorTag(documents: List<Map<String, Any>>) {
        val tag = documents.lastOrNull()?.get("tag") as? String ?: return
        cursorTag?.let {
            if (tag < it) return
        }
        cursorTag = tag
    }

    // Delete UserConversation
    fun deleteUserConversation(itemModel: DynamicUIItem) {
        val conversation = itemModel.data as? Conversation ?: return
        if (!conversation.isValid()) return
        val id = conversation.id
        _isLoading.value = true

        viewModelScope.launch {
            conversationRepo.deleteUserConversation(id)
            _isLoading.value = false
        }
    }

    fun getConversationId(item: DynamicUIItem): String {
        val conversation = item.data as? Conversation ?: return ""
        return conversation.id
    }

    suspend fun getConversationId(messageId: String): String? {
        val message = messageRepo.getMessageFlow(messageId).firstOrNull() ?: return null
        if (!message.isValid()) return null
        return message.conversationId
    }
}