package com.example.medijourney.modules.chat.message

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.MessageMenuAction
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.awaitGet
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
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
import java.util.Calendar
import java.util.UUID

class MessageViewModel : ViewModel() {

    // Properties
    var viewTitle = MutableLiveData<String>()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private val _pinnedItemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val pinnedItemModels: StateFlow<List<DynamicUIItem>> = _pinnedItemModels.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    var shouldScrollToBottom = false
    var didSelectedPinMsg: Boolean = false
    private val _selectedSearchMessageIndex = MutableStateFlow<Int?>(null)
    val selectedSearchMessageIndex: StateFlow<Int?> = _selectedSearchMessageIndex.asStateFlow()
    val messageListReady = MutableLiveData(false)
    private var user: User? = null
    private var conversation: Conversation? = null
    private var messageResults: RealmResults<Message>? = null
    private var userMessageResults: RealmResults<UserMessage>? = null
    private var pinnedMessageResults: RealmResults<Message>? = null
    private var cursorCreatedAt: Long? = null
    private var conversationId: String = ""
    private var latestMessagesQuery: Query? = null
    private var currentPageQuery: MutableList<Query> = mutableListOf()
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    fun onViewCreated(conversationId: String) {
        this.conversationId = conversationId
        viewModelScope.launch {
            getData()
            viewTitle.postValue(getViewTitle(conversation))
            _pinnedItemModels.value = generatePinnedDynamicList(pinnedMessageResults)
            _itemModels.value = generateDynamicList(userMessageResults)
            observeFS()
            launch {
                observePinnedMessagesResults()
            }
            launch {
                observeUserMessages()
            }
            delay(1500)
            messageListReady.postValue(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        latestMessagesQuery?.remove()
        FireStoreManager.removeListeners(this::class.java)
    }

    // Functions
    private suspend fun getData() {
        user = FirebaseAuthManager.getCurrentRealmUser()
        getConversation()
        getMessageResults()
        getUserMessageResults()
        getPinnedMessageResults()
    }

    private fun getViewTitle(conversation: Conversation?): String {
        conversation ?: return ""
        if (!conversation.isValid()) return ""

        return conversation.name
    }

    private suspend fun getConversation() {
        conversation = RealmManager.read(Conversation::class.java, conversationId)
    }

    private suspend fun getMessageResults() {
        messageResults = RealmManager.read(
            clazz = Message::class.java,
            realmQuery = RQuery.Where(Message::conversationId.name, Operator.EQUAL, conversationId),
            sort = listOf(
                Message::createdAt.name to Sort.DESCENDING
            )
        )
    }

    private suspend fun getUserMessageResults() {
        userMessageResults = RealmManager.read(
            clazz = UserMessage::class.java,
            realmQuery = RQuery.Where(UserMessage::conversationId.name, Operator.EQUAL, conversationId),
            sort = listOf(
                UserMessage::createdAt.name to Sort.DESCENDING
            )
        )
    }

    private suspend fun getPinnedMessageResults() {
        pinnedMessageResults = RealmManager.read(
            clazz = Message::class.java,
            realmQuery = RQuery.Where(Message::isPinned.name, Operator.EQUAL, true),
            sort = listOf(
                Message::createdAt.name to Sort.DESCENDING
            )
        )
    }

    private fun observeFS() {
        observePinnedMessages()
        observeLatestMessages()
    }

    private fun observePinnedMessages() {
        FireStoreManager.buildCollectionRef(FireStoreCollection.MESSAGES)
            .whereEqualTo("conversation_id", conversationId)
            .whereEqualTo("is_pinned", true)
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .observe(Message::class.java, this::class.java)
    }

    private fun observeLatestMessages() {
        latestMessagesQuery = FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MESSAGES)
            .whereEqualTo("conversation_id", conversationId)
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .limit(Constants.DEFAULT_LIMIT)

        latestMessagesQuery?.addListener {
            if (cursorCreatedAt == null) {
                updateCursorCreatedAt(it.documents)
            }
            saveDocuments(it.documents)
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observePinnedMessagesResults() {
        val messages = pinnedMessageResults ?: return

        messages.asFlow()
            .debounce(500)
            .collect {
                this.pinnedMessageResults = it.list
                _pinnedItemModels.value = generatePinnedDynamicList(pinnedMessageResults)
            }

    }

    @Suppress("NAME_SHADOWING")
    @OptIn(FlowPreview::class)
    private suspend fun observeUserMessages() {
        val messageResults = messageResults ?: return
        val userMessageResults = userMessageResults ?: return

        combine(
            messageResults.asFlow(),
            userMessageResults.asFlow()
        ) { messageResults, userMessageResults ->
            this.messageResults = messageResults.list
            this.userMessageResults = userMessageResults.list
        }
            .debounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicList(this.userMessageResults)
            }
    }

    private fun generatePinnedDynamicList(pinnedMessageResults: RealmResults<Message>?): List<DynamicUIItem> {
        pinnedMessageResults ?: return emptyList()
        val size = pinnedMessageResults.size

        return pinnedMessageResults.mapIndexedNotNull { index, entity ->
            val message = entity.message ?: return@mapIndexedNotNull null
            if (!entity.isValid()) return@mapIndexedNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                groupIndex = 0,
                itemTag = entity.id,
                data = entity,
                backgroundColor = 0
            ).apply {
                title = MTextStyle("${index + 1} / $size")
                description = MTextStyle(message)
            }
        }
    }

    private fun generateDynamicList(userMessageResults: RealmResults<UserMessage>?): List<DynamicUIItem> {
        userMessageResults ?: return emptyList()
        val size = userMessageResults.size
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode()

        return userMessageResults.mapIndexedNotNull { index, entity ->
            val message = entity.message ?: return@mapIndexedNotNull null
            if (!entity.isValid()) return@mapIndexedNotNull null
            var elderMessage: Message? = null
            val elderIndex = index + 1
            var laterMessage: Message? = null
            val laterIndex = index - 1
            val isComingMessage = currentUserCode != message.senderId

            if (elderIndex < size) {
                elderMessage = userMessageResults[elderIndex].message
            }
            if (laterIndex >= 0) {
                laterMessage = userMessageResults[laterIndex].message
            }
            val isFirstConsecutiveFromUser = checkIsFirstConsecutiveFromUser(message, laterMessage)

            DynamicUIItem(
                type = Constants.ITEM,
                groupIndex = 0,
                itemTag = entity.id,
                data = entity,
                backgroundColor = 0
            ).apply {
                if (isFirstConsecutiveFromUser && isComingMessage) {
                    image =  ImageStyle(url = "images/${message.senderId}/${message.senderImageName}.jpg")
                }
                generateTitle(isComingMessage, message, elderMessage)?.let {
                    title = it
                }
                message.message?.let { msg ->
                    description = MTextStyle(msg)
                }
                generateSecondaryDescription(message, elderMessage)?.let {
                    secondaryDescription = it
                }
                generatesSecondaryImage(message)?.let {
                    secondaryImage = it
                }
                val mutableMap: MutableMap<String, Any> = mutableMapOf(
                    Constants.IS_INCOMING_MESSAGE to isComingMessage,
                    Constants.IS_FIRST_CONSECUTIVE_FROM_USER to isFirstConsecutiveFromUser,
                    Constants.MESSAGE_MENU_ACTIONS to generateMessageMenuAction(message)
                )
                message.createdAt?.let {
                    mutableMap[Constants.MESSAGE_DATE] = DateHelper.convertRealmInstantToString(it, Constants.DATE_FORMAT_2)
                }
                additionalData = mutableMap
            }
        }
    }

    private fun checkIsFirstConsecutiveFromUser(message: Message, laterMessage: Message?): Boolean {
        if (!message.isValid()) return false

        return if (laterMessage != null && laterMessage.isValid()) {
            if (laterMessage.senderId == message.senderId) {
                val createdAt = message.createdAt ?: return false
                val date = DateHelper.convertRealmInstantToDate(createdAt)
                val elderCreatedAt = laterMessage.createdAt ?: return false
                val elderDate = DateHelper.convertRealmInstantToDate(elderCreatedAt)

                !DateHelper.compareEqualDates(date, elderDate, setOf(Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR))
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun generateTitle(isComingMessage: Boolean, message: Message, elderMessage: Message?) : MTextStyle? {
        if (!message.isValid()) return null
        if (!isComingMessage) return null
        val senderName = message.senderName ?: return null
        val title = MTextStyle(senderName)

        return if (elderMessage != null)  {
            if (!elderMessage.isValid()) return null
            if (elderMessage.senderId == message.senderId) return null
            title
        } else {
            title
        }
    }

    private fun generatesSecondaryImage(message: Message) : ImageStyle? {
        if (!message.isValid()) return null

        val url = message.mediaName?.let {
            "images/${conversationId}/${message.mediaName}.jpg"
        } ?: run {
            message.mediaUrl
        }

        return when (message.mediaType) {
            "image" -> {
                ImageStyle(url = url)
            }
            else -> {
                null
            }
        }
    }

    private fun generateSecondaryDescription(message: Message, elderMessage: Message?): MTextStyle? {
        if (!message.isValid()) return null
        val createdAt = message.createdAt ?: return null
        val date = DateHelper.convertRealmInstantToDate(createdAt)

        return elderMessage?.let {
            val elderCreatedAt = it.createdAt ?: return null
            val elderDate = DateHelper.convertRealmInstantToDate(elderCreatedAt)
            if (DateHelper.compareEqualDates(date, elderDate, setOf(Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR))) return null

            MTextStyle(DateHelper.convertDateToString(date, Constants.DATE_FORMAT_1))
        } ?: run {
            MTextStyle(DateHelper.convertDateToString(date, Constants.DATE_FORMAT_1))
        }
    }

    private fun generateMessageMenuAction(message: Message): List<MessageMenuAction> {
        if (!message.isValid()) return emptyList()

        val actions = mutableListOf(
            MessageMenuAction.DELETE
        )
        if (message.isPinned) {
           actions.add(MessageMenuAction.UNPIN)
        } else {
            actions.add(MessageMenuAction.PIN)
        }
        return actions
    }

    // Send Message
    fun sendMessage(text: String, imageUri: Uri?) {
        val messageDocRef = FireStoreManager.buildDocRef(Pair(FireStoreCollection.MESSAGES, null))
        val userMessageDocRef = FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_MESSAGES, null))
        val messageMap = generateMessageMap(messageDocRef.id, text, imageUri)
        val userMessageMap = generateUserMessageMap(userMessageDocRef.id, messageDocRef.id, messageMap)

        imageUri?.let {
            FirebaseStorageManager.saveImage(it, UUID.randomUUID().toString()) {}
        }
        viewModelScope.launch {
            RealmManager.create(Message::class.java, messageMap)
            RealmManager.create(UserMessage::class.java, userMessageMap)
        }
        messageDocRef.set(messageMap)
        userMessageDocRef.set(userMessageMap)
        shouldScrollToBottom = true
    }

    private fun generateMessageMap(id: String, text: String, imageUri: Uri?): Map<String, Any> {
        val messageMap: MutableMap<String, Any> = mutableMapOf()
        messageMap["message"] = text
        messageMap["id"] = id

        val user = user
        if (user != null && user.isValid()) {
            messageMap["sender_id"] = user.userCode
            messageMap["sender_image_name"] = "avatar"
            user.displayName?.let {
                messageMap["sender_name"] = it
            }
        }
        messageMap["conversation_id"] = conversationId
        messageMap["created_at"] = System.currentTimeMillis()
        imageUri?.let {
            messageMap["media_type"] = "image"
            messageMap["media_url"] = it.toString()
        }
        return messageMap
    }

    @Suppress("NAME_SHADOWING")
    private fun generateUserMessageMap(userMessageId: String,
                                       messageId: String,
                                       json: Map<String, Any>): Map<String, Any> {
        val json = json.toMutableMap()
        json["id"] = userMessageId
        json["message_id"] = messageId
        json.remove("message")
        json.remove("sender_id")
        json.remove("sender_image_name")
        json.remove("sender_name")
        return json
    }

    // Message Menu Actions
    fun handleAction(action: MessageMenuAction, itemModel: DynamicUIItem) {
        when (action) {
            MessageMenuAction.DELETE -> {
                deleteMessage(itemModel)
            }
            MessageMenuAction.PIN -> {
                pinMessage(itemModel, true)
            }
            MessageMenuAction.UNPIN -> {
                pinMessage(itemModel, false)
            }
        }
    }

    // Delete Message
    private fun deleteMessage(itemModel: DynamicUIItem) {
        val userMessage = itemModel.data as? UserMessage ?: return
        if (!userMessage.isValid()) return
        val message = userMessage.message ?: return
        if (!message.isValid()) return
        val userMessageId = userMessage.id
        val messageId = message.id
        _isLoading.value = true

        FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_MESSAGES, userMessageId))
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.delete(UserMessage::class.java, userMessageId)
                    RealmManager.delete(Message::class.java, messageId)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    // Pin Message
    private fun pinMessage(itemModel: DynamicUIItem, isPinned: Boolean) {
        val userMessage = itemModel.data as? UserMessage ?: return
        if (!userMessage.isValid()) return
        val message = userMessage.message ?: return
        if (!message.isValid()) return

        FireStoreManager.buildDocRef(Pair(FireStoreCollection.MESSAGES, message.id))
            .update("is_pinned", isPinned)
            .addOnSuccessListener {
                viewModelScope.launch {
                    RealmManager.update(Message::class.java, message.id, mapOf("is_pinned" to isPinned))
                }
            }
    }

    fun getNextPinnedMessageIndex(currentIndex: Int?): Int {
        if (currentIndex == null) return 0
        if (currentIndex > _pinnedItemModels.value.size - 1) return 0

        return if (currentIndex == _pinnedItemModels.value.size - 1) {
            0
        } else {
            currentIndex + 1
        }
    }

    fun getPinnedUserMessageIndex(pinnedMessageIndex: Int): Int {
        if (pinnedMessageIndex > _pinnedItemModels.value.size - 1) return 0
        val pinnedMessage = _pinnedItemModels.value[pinnedMessageIndex].data as? Message ?: return 0
        if (!pinnedMessage.isValid()) return 0

        return _itemModels.value.indexOfFirst {
            val userMessage = it.data as? UserMessage ?: return@indexOfFirst false
            val message = userMessage.message ?: return@indexOfFirst false
            message.isValid() && message.id == pinnedMessage.id
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val dateLong = getDateLong(index) ?: return
        val cursorCreatedAt = cursorCreatedAt ?: return
        if (dateLong >= cursorCreatedAt) return

        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null
        viewModelScope.launch {
            getCurrentPage(dateLong)
        }
    }

    private fun getDateLong(index: Int): Long? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val userMessage = item.data as? UserMessage ?: return null
        if (!userMessage.isValid()) return null
        val createdAt = userMessage.createdAt ?: return null

        return DateHelper.convertRealmInstantToMillis(createdAt)
    }

    private suspend fun getCurrentPage(dateLong: Long) {
        try {
            val snapshots = FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MESSAGES)
                .whereEqualTo("conversation_id", conversationId)
                .whereGreaterThan(Constants.CREATED_AT, dateLong)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
                .awaitGet()

            updateCursorCreatedAt(snapshots.documents)
            saveDocuments(snapshots.documents)
            observeCurrentPage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            val dateLong = cursorCreatedAt ?: return@launch
            FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MESSAGES)
                .whereEqualTo("conversation_id", conversationId)
                .whereGreaterThan(Constants.CREATED_AT, dateLong)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
                .also {
                    currentPageQuery.add(it)
                    it.addListener { snapshots ->
                        updateCursorCreatedAt(snapshots.documents)
                        saveDocuments(snapshots.documents)
                    }
                }

            FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MESSAGES)
                .whereEqualTo("conversation_id", conversationId)
                .whereLessThan(Constants.CREATED_AT, dateLong)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
                .also {
                    currentPageQuery.add(it)
                    it.addListener { snapshots ->
                        saveDocuments(snapshots.documents)
                    }
                }
        }
    }

    private fun updateCursorCreatedAt(documents: List<DocumentSnapshot>) {
        val firstDoc = documents.firstOrNull() ?: return
        val firstData = firstDoc.data ?: return
        val latestCreatedDateNumber = firstData[Constants.CREATED_AT] as? Long ?: return

        val lastDoc = documents.lastOrNull() ?: return
        val lastData = lastDoc.data ?: return
        val eldestCreatedDateNumber = lastData[Constants.CREATED_AT] as? Long ?: return

        val cursorDate = cursorCreatedAt

        if (didSelectedPinMsg || _selectedSearchMessageIndex.value != null) {
            if (cursorDate == null) return
            if (cursorDate > latestCreatedDateNumber) return

            cursorCreatedAt = eldestCreatedDateNumber
        } else {
            if (cursorDate != null && eldestCreatedDateNumber > cursorDate) return

            cursorCreatedAt = eldestCreatedDateNumber
        }
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(UserMessage::class.java, data)
                }
            }
        }
    }

    fun updateSelectedSearchMessageIndex(userMessageId: String) {
        val userMessages = userMessageResults ?: return
        val index = userMessages.indexOfFirst {
            it.isValid() && it.id == userMessageId
        }
        if (index < 0) return
        _selectedSearchMessageIndex.value = index
    }
}