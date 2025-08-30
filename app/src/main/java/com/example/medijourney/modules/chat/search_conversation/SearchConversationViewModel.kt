package com.example.medijourney.modules.chat.search_conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.UserConversation
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchConversationViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var conversationResults: RealmResults<Conversation>? = null
    private var observeConversationsJob: Job? = null
    private var cursorTag: String? = null
    private var firstPageQuery: Query? = null
    private var currentPageQuery: MutableList<Query> = mutableListOf()
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            getData()
            _itemModels.value = generateDynamicUIItemModels(conversationResults)
            observeFS { _isLoading.value = false }
            observeConversationsJob = launch { observeData() }
            launch { observeSearchText() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeAllFSListeners()
    }

    // Functions
    private suspend fun getData(keywords: String? = null) {
        val queryList: MutableList<RQuery> = mutableListOf()

        queryList.add(RQuery.Where(Conversation::isAdded.name, Operator.EQUAL, false))
        if (!keywords.isNullOrEmpty()) {
            queryList.add(RQuery.Where(Conversation::keywords.name, Operator.CONTAINS, keywords))
        }
        conversationResults = RealmManager.read(
            clazz = Conversation::class.java,
            realmQuery = RQuery.And(queryList),
            sort = listOf(Conversation::tag.name to Sort.ASCENDING)
        )
    }

    private fun observeFS(keywords: String? = null, completion: (Boolean) -> Unit) {
        firstPageQuery = FireStoreManager.buildCollection(FireStoreCollection.CONVERSATIONS)
            .apply {
                if (!keywords.isNullOrEmpty()) {
                    whereArrayContains(Conversation::keywords.name, keywords)
                }
            }
            .orderBy(Conversation::tag.name)
            .limit(Constants.DEFAULT_LIMIT)

        firstPageQuery?.addListener {
            updateCursorTag(it.documents)
            saveDocuments(it.documents)
            completion.invoke(true)
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val conversations = conversationResults ?: return

        conversations.asFlow()
            .debounce(500)
            .collect {
                conversationResults = it.list
                _itemModels.value = generateDynamicUIItemModels(conversationResults)
            }
    }

    private fun generateDynamicUIItemModels(conversations: RealmResults<Conversation>?): List<DynamicUIItem> {
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
            getData(keyword)
            observeFS(keyword) { _isLoading.value = false }
            observeConversationsJob = launch { observeData() }
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
        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val conversationTag = getConversationTag(index) ?: return
        val conversationCursorTag = cursorTag ?: return
        if (conversationTag <= conversationCursorTag) return
        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null
        getCurrentPage(conversationTag)
    }

    private fun getConversationTag(index: Int): String? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val conversation = item.data as? Conversation ?: return null
        if (!conversation.isValid()) return null

        return conversation.tag
    }

    private fun getCurrentPage(conversationTag: String) {
        FireStoreManager.buildCollection(FireStoreCollection.CONVERSATIONS)
            .whereGreaterThan(Conversation::tag.name, conversationTag)
            .orderBy(Conversation::tag.name)
            .limit(Constants.DEFAULT_LIMIT)
            .get()
            .addOnSuccessListener {
                updateCursorTag(it.documents)
                saveDocuments(it.documents)
                observeCurrentPage()
            }
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            cursorTag?.let { tag ->

                FireStoreManager.buildCollection(FireStoreCollection.CONVERSATIONS)
                    .whereGreaterThan(Conversation::tag.name, tag)
                    .orderBy(Conversation::tag.name)
                    .limit(Constants.DEFAULT_LIMIT)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            updateCursorTag(snapshots.documents)
                            saveDocuments(snapshots.documents)
                        }
                    }

                FireStoreManager.buildCollection(FireStoreCollection.CONVERSATIONS)
                    .whereLessThanOrEqualTo(Conversation::tag.name, tag)
                    .orderBy(Conversation::tag.name)
                    .limit(Constants.DEFAULT_LIMIT)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            saveDocuments(snapshots.documents)
                        }
                    }
            }
        }
    }

    private fun updateCursorTag(documents: List<DocumentSnapshot>) {
        val lastDocument = documents.lastOrNull() ?: return
        val data = lastDocument.data ?: return
        val tag = data["tag"] as? String ?: return
        val cursorTag = cursorTag
        if (cursorTag != null && tag < cursorTag) return

        this.cursorTag = tag
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(Conversation::class.java, data)
                }
            }
        }
    }

    // Add Conversation
    fun addUserConversation(itemModel: DynamicUIItem, completion: (Boolean) -> Unit) {
        val conversation = itemModel.data as? Conversation ?: return
        val conversationId = conversation.id
        if (!conversation.isValid()) return

        val map: MutableMap<String, Any> = mutableMapOf()
        map["tag"] = conversation.tag
        map["conversation_id"] = conversation.id

        val docRef = FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_CONVERSATIONS, null))
        map["id"] = docRef.id
        FirebaseAuthManager.getCurrentUserCode()?.let {
            map["user_code"] = it
        }

        docRef.set(map).addOnCompleteListener {
            if (it.isSuccessful) {
                viewModelScope.launch {
                    RealmManager.update(Conversation::class.java, conversationId, mapOf("is_added" to true))
                    RealmManager.create(UserConversation::class.java, map)
                    completion.invoke(true)
                }
            } else {
                completion.invoke(false)
            }
        }
    }
}