package com.example.medijourney.modules.chat.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.UserConversation
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.example.medijourney.common.respositories.UserConversationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainChatViewModel @Inject constructor(
    private val userConversationRepository: UserConversationRepo,
    conversationRepository: ConversationRepo
) : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var userConversationsFlow = userConversationRepository.getUserConversationsFlow(null)
    private var conversationsFlow = conversationRepository.getConversationsFlow()
    private var userConversationCursorTag: String? = null
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
        userConversationRepository.observeUserConversations()
            .collect {
                updateUserConversationCursorTag(it)
            }
    }

    private suspend fun observeData() {
        userConversationsFlow
            .combine(conversationsFlow) { userConversations, conversations ->
                Pair(userConversations, conversations)
            }
            .firstThenDebounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicUIItemModels(it.first, it.second)
            }
    }

    private fun generateDynamicUIItemModels(
        userConversations: List<UserConversation>?,
        conversations: List<Conversation>?
    ): List<DynamicUIItem> {
        userConversations ?: return emptyList()
        conversations ?: return emptyList()
        return userConversations.mapNotNull {
            if (!it.isValid()) return@mapNotNull null
            val conversation = it.conversation ?: return@mapNotNull null
            if (!conversation.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
            ).apply {
                conversation.imageName?.let { imageName ->
                    image = ImageStyle(url = "images/conversations/${imageName}.jpg", name = "ic_group")
                } ?: run {
                    image = ImageStyle(name = "ic_group")
                }
                title = MTextStyle(conversation.name)
                conversation.shortTag?.let { shortTag ->
                    description = MTextStyle(shortTag)
                }
                conversation.lastMessage?.let { lastMessage ->
                    description = MTextStyle(lastMessage)
                }
            }
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        if (index >= _itemModels.value.size) return
        val item = _itemModels.value[index]
        val userConversation = item.data as? UserConversation ?: return
        if (!userConversation.isValid()) return
        val userConversationCursorTag = userConversationCursorTag ?: return
        if (userConversation.tag < userConversationCursorTag) return

        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        viewModelScope.launch {
            getCurrentPage(userConversation.tag)
        }
    }

    private suspend fun getCurrentPage(tag: String) {
        val dataList = userConversationRepository.getUserConversationsPageAfterTag(tag)
        updateUserConversationCursorTag(dataList)
        observeCurrentPage()
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            val tag = userConversationCursorTag ?: return@launch

            userConversationRepository.observeUserConversationsPageAfterTag(tag)
                .collect {
                    updateUserConversationCursorTag(it)
                }
            userConversationRepository.observeUserConversationsPageBeforeTag(tag)
                .collect()
        }
    }

    private fun updateUserConversationCursorTag(documents: List<MutableMap<String, Any>>) {
        val tag = documents.lastOrNull()?.get("tag") as? String ?: return
        userConversationCursorTag?.let {
            if (tag < it) return
        }

        userConversationCursorTag = tag
    }

    // Delete UserConversation
    fun deleteUserConversation(itemModel: DynamicUIItem) {
        val userConversation = itemModel.data as? UserConversation ?: return
        if (!userConversation.isValid()) return
        val id = userConversation.id
        val conversationId = userConversation.conversationId
        val tag = userConversation.tag
        _isLoading.value = true

        viewModelScope.launch {
            if (tag == userConversationCursorTag) {
                viewModelScope.launch {
                    val entity = RealmManager.read(
                        clazz = UserConversation::class.java,
                        realmQuery = RQuery.Where(UserConversation::tag.name, Operator.LESS_THAN, tag)
                    ).lastOrNull()
                    if (entity != null && entity.isValid()) {
                        userConversationCursorTag = entity.tag
                    }
                }
            }
        }

        FireStoreManager.buildUserDocRef(FireStoreCollection.USER_CONVERSATIONS to id)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.delete(UserConversation::class.java, id)
                    RealmManager.update(
                        clazz = Conversation::class.java,
                        conversationId,
                        mapOf("is_added" to false)
                    )
                    RealmManager.delete(
                        clazz = UserMessage::class.java,
                        realmQuery = RQuery.Where(UserMessage::conversationId.name, Operator.EQUAL, conversationId)
                    )
                    RealmManager.delete(
                        clazz = Message::class.java,
                        realmQuery = RQuery.Where(Message::conversationId.name, Operator.EQUAL, conversationId)
                    )
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun getConversationId(item: DynamicUIItem): String {
        val userConversation = item.data as? UserConversation ?: return ""
        return userConversation.conversationId
    }

    suspend fun getConversationId(userMessageId: String): String? {
        val userMessage = RealmManager.read(
            clazz = UserMessage::class.java,
            userMessageId
        )
        return userMessage?.conversationId
    }
}