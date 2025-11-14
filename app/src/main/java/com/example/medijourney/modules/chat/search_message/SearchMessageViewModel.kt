package com.example.medijourney.modules.chat.search_message

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.awaitGet
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.example.medijourney.common.respositories.MessageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchMessageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepo: ConversationRepo,
    private val messageRepo: MessageRepo
) : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var conversationId: String? = savedStateHandle.get<String>("conversationId")
    private var dateCursor: Long? = null
    private var observeMessagesJob: Job? = null

    // Companion
    companion object {
        const val TEXT_LIST = "text_list"
    }

    // View cycle
    init {
        viewModelScope.launch {
            observeMessagesFlow()
            launch {
                observeSearchText()
            }
        }
    }

    private fun observeMessagesFlow() {
        observeMessagesJob?.let {
            it.cancel()
            observeMessagesJob = null
        }
        observeMessagesJob = viewModelScope.launch {
            launch {
                messageRepo
                    .listenMessages(conversationId, searchTextFlow.value, null)
                    .collect {
                        updateDateCursor(it)
                    }
            }
            launch {
                messageRepo
                    .geMessagesFLow(conversationId, searchTextFlow.value)
                    .firstThenDebounce(500)
                    .collect {
                        _itemModels.value = generateDynamicUIItemModels(it)
                    }
            }
        }
    }

    // Functions
    private fun generateDynamicUIItemModels(messages: List<Message>?): List<DynamicUIItem> {
        if (messages == null) return emptyList()

        return messages.mapNotNull { message ->
            if (!message.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = message.id,
                groupIndex = 0,
                data = message,
                backgroundColor = 0
            ).apply {
                message.senderName?.let {
                    title = MTextStyle(it)
                }
                message.createdAt?.let {
                    description =  MTextStyle(DateHelper.convertDateToString(it, Constants.DATE_FORMAT_3))
                }
                additionalData = mapOf(
                    TEXT_LIST to generateMTextStyleList(
                        original = message.message ?: "",
                        keyword = searchTextFlow.value ?: "",
                        defaultColor = R.color.transparent,
                        keywordColor = R.color.corn_flower_blue_color
                    )
                )
            }
        }
    }

    private fun generateMTextStyleList(
        original: String,
        keyword: String,
        defaultColor: Int,
        keywordColor: Int
    ): List<MTextStyle> {
        val result = mutableListOf<MTextStyle>()

        if (keyword.isEmpty()) {
            return listOf(MTextStyle(text = original, color = defaultColor))
        }

        var startIndex = 0
        while (true) {
            val index = original.indexOf(keyword, startIndex)
            if (index == -1) {
                if (startIndex < original.length) {
                    result.add(MTextStyle(
                        text = original.substring(startIndex),
                        color = defaultColor)
                    )
                }
                break
            }
            if (index > startIndex) {
                result.add(MTextStyle(
                    text = original.substring(startIndex, index),
                    color = defaultColor)
                )
            }
            result.add(MTextStyle(
                text = keyword,
                color = keywordColor)
            )
            startIndex = index + keyword.length
        }

        return result
    }

    // Search Messages
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                searchMessage()
            }
    }

    private fun searchMessage() {
        _isLoading.value = true
        dateCursor = null
        viewModelScope.launch {
            observeMessagesFlow()
            _isLoading.value = false
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        if (index >= _itemModels.value.size) return
        val item = _itemModels.value[index]
        val message = item.data as? Message ?: return
        if (!message.isValid()) return
        val cursorCreatedAt = dateCursor ?: return
        val createdAt = message.createdAt ?: return
        val dateLong = DateHelper.convertRealmInstantToMillis(createdAt)
        if (dateLong >= cursorCreatedAt) return

        viewModelScope.launch {
            messageRepo
                .listenMessages(conversationId, searchTextFlow.value, dateLong)
                .collect {
                    updateDateCursor(it)
                }
        }
    }

    private fun updateDateCursor(documents: List<Map<String, Any>>) {
        val lastData = documents.lastOrNull() ?: return
        val eldestCreatedDateNumber = lastData[Constants.CREATED_AT] as? Long ?: return
        val eldestCreatedDateLong = eldestCreatedDateNumber
        val cursorDate = dateCursor
        if (cursorDate != null && eldestCreatedDateLong > cursorDate) return

        dateCursor = eldestCreatedDateLong
    }

    // Selected Message Handle
    fun handleSearchMessageClick(itemModel: DynamicUIItem, completion: (String?) -> Unit) {
        val message = itemModel.data as? Message ?: return completion(null)
        if (!message.isValid()) return completion(null)
        val messageId = message.id
        val conversationId = message.conversationId

        viewModelScope.launch {
            if (conversationRepo.getConversationFlow(conversationId).firstOrNull() == null) {
                val convDeferred = async { ensureConversation(conversationId) }
                convDeferred.await()
            }

            val userMessageId = ensureUserMessage(messageId)
            completion(userMessageId)
        }
    }

    private suspend fun ensureConversation(conversationId: String) {
        val existing = conversationRepo.getConversationFlow(conversationId).firstOrNull()
        if (existing != null) {
            if (!existing.includeCurrentUser) return
            RealmManager.update(Conversation::class.java, existing.id, mapOf("is_added" to true))
        } else {
            val snapshot = try {
                FireStoreManager.buildDoc(Pair(FireStoreCollection.CONVERSATIONS, conversationId))
                    .awaitGet()
            } catch (_: Exception) {
                return
            }

            snapshot.data?.let {
                it["include_current_user"] = true
                RealmManager.create(Conversation::class.java, it)
            }
        }
    }

    private suspend fun ensureUserMessage(messageId: String): String? {
        val existing = messageRepo.getMessageFlow(messageId).firstOrNull() ?: return null
        if (!existing.isValid()) return null

        return existing.id
    }
}