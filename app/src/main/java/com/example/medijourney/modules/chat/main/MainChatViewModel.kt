package com.example.medijourney.modules.chat.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class MainChatViewModel: ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var userConversationResults: RealmResults<UserConversation>? = null
    private var conversationResults: RealmResults<Conversation>? = null
    private var userConversationCursorTag: String? = null
    private var currentPageQuery: MutableList<Query> = mutableListOf()
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            getData()
            _itemModels.value = generateDynamicUIItemModels(userConversationResults, conversationResults)
            observeFS()
            observeData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        FireStoreManager.removeListeners(this::class.java)
    }

    // Functions
    private suspend fun getData() {
        getUserConversationResults()
        getConversationResults()
    }

    private suspend fun getUserConversationResults() {
        userConversationResults = RealmManager.read(
            clazz = UserConversation::class.java,
            sort = listOf(UserConversation::tag.name to Sort.ASCENDING)
        )
    }

    private suspend fun getConversationResults() {
        conversationResults = RealmManager.read(
            clazz = Conversation::class.java,
            sort = listOf(Conversation::tag.name to Sort.ASCENDING)
        )
    }

    private fun observeFS() {
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_CONVERSATIONS)
            .orderBy(UserConversation::tag.name)
            .limit(100)
            .addListener {
                updateUserConversationCursorTag(it.documents)
                saveDocuments(it.documents)
            }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val userConversations = userConversationResults ?: return
        val conversations = conversationResults ?: return

        combine(
            userConversations.asFlow(),
            conversations.asFlow(),
        ) { userConversationsChanges, conversationsChanges ->
            userConversationResults = userConversationsChanges.list
            conversationResults = conversationsChanges.list
        }
            .debounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicUIItemModels(userConversationResults, conversationResults)
        }
    }

    private fun generateDynamicUIItemModels(
        userConversations: RealmResults<UserConversation>?,
        conversations: RealmResults<Conversation>?
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
        currentPageQuery.forEach {
            FireStoreManager.removeListener(it)
        }
        currentPageQuery.clear()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_CONVERSATIONS)
            .whereGreaterThan(Conversation::tag.name, userConversation.tag)
            .orderBy(UserConversation::tag.name)
            .limit(100)
            .get()
            .addOnSuccessListener {
                updateUserConversationCursorTag(it.documents)
                saveDocuments(it.documents)
                observeCurrentPage()
            }
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            userConversationCursorTag?.let { tag ->

                FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_CONVERSATIONS)
                    .whereGreaterThan(Conversation::tag.name, tag)
                    .orderBy(Conversation::tag.name)
                    .limit(100)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            updateUserConversationCursorTag(snapshots.documents)
                            saveDocuments(snapshots.documents)
                        }
                    }

                FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_CONVERSATIONS)
                    .whereLessThanOrEqualTo(Conversation::tag.name, tag)
                    .orderBy(Conversation::tag.name)
                    .limit(100)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            saveDocuments(snapshots.documents)
                        }
                    }
            }
        }
    }

    private fun updateUserConversationCursorTag(documents: List<DocumentSnapshot>) {
        val tag = documents.lastOrNull()?.data?.get("tag") as? String ?: return
        userConversationCursorTag?.let {
            if (tag < it) return
        }

        userConversationCursorTag = tag
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(UserConversation::class.java, data)
                }
            }
        }
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