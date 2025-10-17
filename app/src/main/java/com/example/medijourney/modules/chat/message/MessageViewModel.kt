package com.example.medijourney.modules.chat.message

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.MessageMenuAction
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.example.medijourney.common.respositories.MessageRepo
import com.example.medijourney.common.respositories.UserMessageRepo
import com.example.medijourney.common.respositories.UserRepo
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    state: SavedStateHandle,
    private val userRepository: UserRepo,
    conversationRepository: ConversationRepo,
    private val userMessageRepository: UserMessageRepo,
    private val messageRepository: MessageRepo
) : ViewModel() {

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
    private var conversationId: String = state.get<String>("conversationId") ?: ""
    private var user: User? = null
    private var conversationFlow = conversationRepository.getConversationFlow(conversationId)
    private var messagesFlow = messageRepository.geMessagesFLow(conversationId)
    private var userMessagesFlow = userMessageRepository.getUserMessageFLow(conversationId)
    private var pinnedMessagesFlow = messageRepository.getPinnedMessagesFlow(conversationId)
    private var cursorCreatedAt: Long? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            user = userRepository.getUserFlow(FAManger.currentUserCode).firstOrNull()
            observeData(this)
            observeFS(this)
            delay(1500)
            messageListReady.postValue(true)
        }
    }

    private fun observeData(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            conversationFlow.collect {
                val conversation = it ?: return@collect
                val title = getViewTitle(conversation)

                viewTitle.postValue(title)
            }
        }

        coroutineScope.launch {
            messagesFlow
                .combine(userMessagesFlow) { _, userMessages -> userMessages }
                .firstThenDebounce(500)
                .collectLatest {
                    _itemModels.value = generateDynamicList(it)
                }
        }

        coroutineScope.launch {
            pinnedMessagesFlow
                .firstThenDebounce(500)
                .collect {
                _pinnedItemModels.value = generatePinnedDynamicList(it)
            }
        }
    }

    // Functions
    private fun getViewTitle(conversation: Conversation?): String {
        conversation ?: return ""
        if (!conversation.isValid()) return ""

        return conversation.name
    }


    private fun observeFS(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            messageRepository.observePinnedMessages(conversationId)
        }
        coroutineScope.launch {
            userMessageRepository.observeLatestMessages(conversationId)
                .collect {
                    updateCursorCreatedAt(it)
                }
        }
    }

    private fun generatePinnedDynamicList(pinnedMessageResults: List<Message>?): List<DynamicUIItem> {
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

    private fun generateDynamicList(userMessageResults: List<UserMessage>?): List<DynamicUIItem> {
        userMessageResults ?: return emptyList()
        val size = userMessageResults.size
        val currentUserCode = FAManger.currentUserCode

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
        val messageDocRef = FireStoreManager.buildDoc(Pair(FireStoreCollection.MESSAGES, null))
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
            messageMap["sender_id"] = user.id
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

        FireStoreManager.buildDoc(Pair(FireStoreCollection.MESSAGES, message.id))
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
        val dataList = userMessageRepository.getOlderMessages(conversationId, dateLong)
        updateCursorCreatedAt(dataList)
        observeCurrentPage()
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            val dateLong = cursorCreatedAt ?: return@launch
            userMessageRepository.observeOlderMessages(conversationId, dateLong)
                .collect {
                    updateCursorCreatedAt(it)
                }
            userMessageRepository.observeLaterMessages(conversationId, dateLong)
                .collect()
        }
    }

    private fun updateCursorCreatedAt(documents: List<Map<String, Any>>) {
        val firstDoc = documents.firstOrNull() ?: return
        val latestCreatedDateNumber = firstDoc[Constants.CREATED_AT] as? Long ?: return
        val lastDoc = documents.lastOrNull() ?: return
        val eldestCreatedDateNumber = lastDoc[Constants.CREATED_AT] as? Long ?: return

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

    fun updateSelectedSearchMessageIndex(userMessageId: String) {
        viewModelScope.launch {
            val entities = userMessagesFlow.firstOrNull()
            val index = entities?.indexOfFirst {
                it.isValid() && it.id == userMessageId
            }
            if (index != null && index >= 0) {
                _selectedSearchMessageIndex.value = index
            }
        }
    }
}