package com.example.medijourney.modules.chat.search_message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.awaitGet
import com.example.medijourney.common.managers.fire_store.awaitSet
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.UserConversation
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchMessageViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var userConversation: UserConversation? = null
    private var userConversations: RealmResults<UserConversation>? = null
    private var messages: RealmResults<Message>? = null
    private var conversationIds = listOf<String>()
    private var cursorCreatedAt: Long? = null
    private var observeMessagesJob: Job? = null

    // Companion
    companion object {
        const val TEXT_LIST = "text_list"
    }

    // View cycle
    fun onViewCreated(userConversationId: String?) {
        viewModelScope.launch {
            getData(userConversationId)
            _itemModels.value = generateDynamicUIItemModels(messages)
            getFSUserConversation()
            launch { observeSearchText() }
            observeMessagesJob = launch { observeData()}
        }
    }

    // Functions
    private suspend fun getData(userConversationId: String?) {
        userConversationId?.let {
            userConversation = RealmManager.read(UserConversation::class.java, userConversationId)
        } ?: run {
            userConversations = RealmManager.read(UserConversation::class.java)
        }
        getMessages(searchTextFlow.value)
    }

    private suspend fun getMessages(keywords: String?, userConversation: UserConversation? = null) {
        if (keywords.isNullOrEmpty()) return
        val queryList: MutableList<RQuery> = mutableListOf()

        if (userConversation != null && userConversation.isValid()) {
            queryList.add(RQuery.Where(Message::conversationId.name, Operator.EQUAL, userConversation.conversationId))
        }
        queryList.add(RQuery.Where(Message::keywords.name, Operator.CONTAINS, keywords))

        messages = RealmManager.read(
            clazz = Message::class.java,
            realmQuery = if (queryList.isNotEmpty()) RQuery.And(queryList) else null,
            sort = listOf(
                Message::createdAt.name to Sort.DESCENDING
            )
        )
    }

    private fun getFSUserConversation() {
        if (userConversation != null) return

        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_CONVERSATIONS)
            .get()
            .addOnSuccessListener { snapshot ->
                conversationIds = snapshot.documents.mapNotNull {
                    val data = it.data ?: return@mapNotNull null
                    data["conversation_id"] as? String
                }
                getFSMessage(searchTextFlow.value, userConversation, conversationIds)
            }
    }

    private fun getFSMessage(keywords: String? = null, userConversation: UserConversation?, conversationIds: List<String>) {
        FireStoreManager.buildCollectionRef(FireStoreCollection.MESSAGES)
            .apply {
                if (userConversation != null && userConversation.isValid()) {
                    whereEqualTo("conversation_id", userConversation.id)
                } else if (conversationIds.isNotEmpty()) {
                    whereIn("conversation_id", conversationIds)
                }
                if (!keywords.isNullOrEmpty()) {
                    whereArrayContains("keywords", keywords)
                }
            }
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .limit(Constants.DEFAULT_LIMIT)
            .get()
            .addOnSuccessListener { snapshot ->
            updateCursorCreatedAt(snapshot.documents)
            saveDocuments(snapshot.documents)
            _isLoading.value = false
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val msg = messages ?: return

        msg.asFlow()
            .debounce(500)
            .collect {
                messages = it.list
                _itemModels.value = generateDynamicUIItemModels(messages)
            }
    }

    private fun generateDynamicUIItemModels(messages: RealmResults<Message>?): List<DynamicUIItem> {
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
                searchMessage(it)
            }
    }

    private fun searchMessage(keywords: String?) {
        _isLoading.value = true
        cursorCreatedAt = null
        observeMessagesJob?.cancel()
        observeMessagesJob = null
        viewModelScope.launch {
            getMessages(keywords, userConversation)
            getFSMessage(keywords, userConversation, conversationIds)
            observeMessagesJob = launch { observeData() }
            _itemModels.value = generateDynamicUIItemModels(messages)
            _isLoading.value = false
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        if (index >= _itemModels.value.size) return
        val item = _itemModels.value[index]
        val message = item.data as? Message ?: return
        if (!message.isValid()) return
        val cursorCreatedAt = cursorCreatedAt ?: return
        val createdAt = message.createdAt ?: return
        val dateLong = DateHelper.convertRealmInstantToMillis(createdAt)
        if (dateLong >= cursorCreatedAt) return

        FireStoreManager.buildCollectionRef(FireStoreCollection.MESSAGES)
            .apply {
                val userConversation = userConversation
                if (userConversation != null && userConversation.isValid()) {
                    whereEqualTo("conversation_id", userConversation.id)
                } else if (conversationIds.isNotEmpty()) {
                    whereIn("conversation_id", conversationIds)
                }
                if (!searchTextFlow.value.isNullOrEmpty()) {
                    whereArrayContains(Message::keywords.name, searchTextFlow.value.toString())
                }
            }
            .whereGreaterThan(Constants.CREATED_AT, cursorCreatedAt)
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .limit(Constants.DEFAULT_LIMIT)
            .get()
            .addOnSuccessListener { snapshot ->
                updateCursorCreatedAt(snapshot.documents)
                saveDocuments(snapshot.documents)
            }
    }

    private fun updateCursorCreatedAt(documents: List<DocumentSnapshot>) {
        val lastDoc = documents.lastOrNull() ?: return
        val lastData = lastDoc.data ?: return
        val eldestCreatedDateNumber = lastData[Constants.CREATED_AT] as? Long ?: return
        val eldestCreatedDateLong = eldestCreatedDateNumber as? Long ?: return
        val cursorDate = cursorCreatedAt
        if (cursorDate != null && eldestCreatedDateLong > cursorDate) return

        cursorCreatedAt = eldestCreatedDateLong
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(Message::class.java, data)
                }
            }
        }
    }

    // Selected Message Handle
    fun handleSearchMessageClick(itemModel: DynamicUIItem, completion: (String?) -> Unit) {
        val message = itemModel.data as? Message ?: return completion(null)
        if (!message.isValid()) return completion(null)
        val messageId = message.id
        val conversationId = message.conversationId

        viewModelScope.launch {
            if (userConversation == null) {
                val userConvDeferred = async { ensureUserConversation(conversationId) }
                val convDeferred = async { ensureConversation(conversationId) }
                userConvDeferred.await()
                convDeferred.await()
            }

            val userMessageId = ensureUserMessage(messageId)
            completion(userMessageId)
        }
    }

    private suspend fun ensureConversation(conversationId: String) {
        val existing = RealmManager.read(Conversation::class.java, conversationId)
        if (existing != null) {
            if (!existing.isAdded) return
            RealmManager.update(Conversation::class.java, existing.id, mapOf("is_added" to true))
        } else {
            val snapshot = try {
                FireStoreManager.buildDocRef(Pair(FireStoreCollection.CONVERSATIONS, conversationId))
                    .awaitGet()
            } catch (e: Exception) {
                return
            }

            snapshot.data?.let {
                it["is_added"] = true
                RealmManager.create(Conversation::class.java, it)
            }
        }
    }

    private suspend fun ensureUserConversation(conversationId: String) {
        val existing = RealmManager.read(
            clazz = UserConversation::class.java,
            realmQuery = RQuery.Where(UserConversation::conversationId.name, Operator.EQUAL, conversationId)
        ).firstOrNull()

        if (existing != null) return
        val conversation = RealmManager.read(Conversation::class.java, conversationId) ?: return
        if (!conversation.isValid()) return

        val map: MutableMap<String, Any> = mutableMapOf()
        FirebaseAuthManager.getCurrentUserCode()?.let {
            map["user_code"] = it
        }
        map["conversation_id"] = conversationId
        map["tag"] = conversation.tag

        val data = try {
            FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_CONVERSATIONS, null))
                .awaitSet(map)
        } catch (e: Exception) {
            return
        }

        RealmManager.create(UserConversation::class.java, data)
    }

    private suspend fun ensureUserMessage(messageId: String): String? {
        val existing = RealmManager.read(
            clazz = UserMessage::class.java,
            realmQuery = RQuery.Where(UserMessage::messageId.name, Operator.EQUAL, messageId)
        ).firstOrNull()

        if (existing?.isValid() == true) return existing.id

        val snapshot = try {
            FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MESSAGES)
                .whereEqualTo("message_id", messageId)
                .awaitGet()
        } catch (e: Exception) {
            return null
        }

        val doc = snapshot.documents.firstOrNull() ?: return null
        val data = doc.data ?: return null
        RealmManager.create(UserMessage::class.java, data)
        return doc.id
    }
}