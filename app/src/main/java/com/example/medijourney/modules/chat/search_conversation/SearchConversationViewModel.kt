package com.example.medijourney.modules.chat.search_conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class SearchConversationViewModel @Inject constructor(
    private val conversationRepository: ConversationRepo
) : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var conversationResults: RealmResults<Conversation>? = null
    private var observeConversationsJob: Job? = null
    private var cursorTag: String? = null
    private var firstPageQuery: Query? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            observeConversationsJob = supervisorScope {
                launch {
                 observeFS(searchTextFlow.value)
                }
                launch {
                    observeData(keyword = searchTextFlow.value)
                }
            }
            launch {
                observeSearchText()
            }
        }
    }

    // Functions
    private suspend fun observeFS(keywords: String? = null) {
        conversationRepository.observeConversations(keywords, null)
            .collect {
                updateCursorTag(it)
                _isLoading.value = false
            }
    }

    private suspend fun observeData(keyword: String?) {
        conversationRepository.getConversationsFlow(keyword)
            .firstThenDebounce(500)
            .collect {
                _itemModels.value = generateDynamicUIItemModels(it)
            }
    }

    private fun generateDynamicUIItemModels(conversations: List<Conversation>?): List<DynamicUIItem> {
        conversations ?: return emptyList()
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
                it.description?.let { des ->
                    description = MTextStyle(des)
                }
                it.shortTag?.let { secondaryDes ->
                    secondaryDescription = MTextStyle(secondaryDes)
                }
            }
        }
    }

    // Search Conversations
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                searchConversations(it)
            }
    }

    private fun searchConversations(keyword: String?) {
        _isLoading.value = true
        resetAll()
        viewModelScope.launch {
            observeFS(keyword)
            observeConversationsJob = launch { observeData(searchTextFlow.value) }
            _itemModels.value = generateDynamicUIItemModels(conversationResults)
        }
    }

    private fun resetAll() {
        removeAllFSListeners()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        _itemModels.value = emptyList()
        conversationResults = null
        cursorTag = null
        observeConversationsJob?.cancel()
        observeConversationsJob = null
    }

    private fun removeAllFSListeners() {
        firstPageQuery?.remove()
        firstPageQuery = null
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val conversationTag = getConversationTag(index) ?: return
        val conversationCursorTag = cursorTag ?: return
        if (conversationTag <= conversationCursorTag) return
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = viewModelScope.launch {
            conversationRepository
                .observeConversations(searchTextFlow.value, conversationTag)
                .collect {
                    updateCursorTag(it)
                }
        }
    }

    private fun getConversationTag(index: Int): String? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val conversation = item.data as? Conversation ?: return null
        if (!conversation.isValid()) return null

        return conversation.tag
    }

    private fun updateCursorTag(documents: List<Map<String, Any>>) {
        val data = documents.lastOrNull() ?: return
        val tag = data["tag"] as? String ?: return
        val cursorTag = cursorTag
        if (cursorTag != null && tag < cursorTag) return

        this.cursorTag = tag
    }

    // Add Conversation
    fun addUserConversation(itemModel: DynamicUIItem, completion: (Boolean) -> Unit) {
        val conversation = itemModel.data as? Conversation ?: return
        val conversationId = conversation.id
        if (!conversation.isValid()) return

        val map: MutableMap<String, Any> = mutableMapOf()
        map["tag"] = conversation.tag
        map["conversation_id"] = conversation.id

        viewModelScope.launch {
            val result = FireStoreManager.createDoc(
                FireStoreCollection.USER_CONVERSATIONS,
                null,
                map
            )
            RealmManager.update(
                Conversation::class,
                queryBuilder = RQueryBuilder().equalTo("id", conversationId),
                mapOf("is_added" to true)
            )
            completion.invoke(result)
        }
    }
}