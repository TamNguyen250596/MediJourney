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
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.ConversationRepo
import com.example.medijourney.common.respositories.MessageRepo
import com.example.medijourney.common.respositories.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val messageRepo: MessageRepo
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
    private var messagesFlow = messageRepo.geMessagesFLow(conversationId, null)
    private var pinnedMessagesFlow = messageRepo.getPinnedMessagesFlow(conversationId)
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
            messageRepo
                .listenMessages(conversationId, null, null)
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

    private fun generateDynamicList(messages: List<Message>): List<DynamicUIItem> {
        val size = messages.size
        val currentUserCode = FAManger.currentUserCode

        return messages.mapIndexedNotNull { index, entity ->
            if (!entity.isValid()) return@mapIndexedNotNull null
            var elderMessage: Message? = null
            val elderIndex = index + 1
            var laterMessage: Message? = null
            val laterIndex = index - 1
            val isComingMessage = currentUserCode != entity.senderId

            if (elderIndex < size) {
                elderMessage = messages[elderIndex]
            }
            if (laterIndex >= 0) {
                laterMessage = messages[laterIndex]
            }
            val isFirstConsecutiveFromUser = checkIsFirstConsecutiveFromUser(entity, laterMessage)

            DynamicUIItem(
                type = Constants.ITEM,
                groupIndex = 0,
                itemTag = entity.id,
                data = entity,
                backgroundColor = 0
            ).apply {
                if (isFirstConsecutiveFromUser && isComingMessage) {
                    image =  ImageStyle(url = "images/${entity.senderId}/${entity.senderImageName}.jpg")
                }
                generateTitle(isComingMessage, entity, elderMessage)?.let {
                    title = it
                }
                entity.message?.let { msg ->
                    description = MTextStyle(msg)
                }
                generateSecondaryDescription(entity, elderMessage)?.let {
                    secondaryDescription = it
                }
                generatesSecondaryImage(entity)?.let {
                    secondaryImage = it
                }
                val mutableMap: MutableMap<String, Any> = mutableMapOf(
                    Constants.IS_INCOMING_MESSAGE to isComingMessage,
                    Constants.IS_FIRST_CONSECUTIVE_FROM_USER to isFirstConsecutiveFromUser,
                    Constants.MESSAGE_MENU_ACTIONS to generateMessageMenuAction(entity)
                )
                entity.createdAt?.let {
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
        val messageMap = generateMessageMap(text, imageUri)

        imageUri?.let {
            FirebaseStorageManager.saveImage(it, UUID.randomUUID().toString()) {}
        }
        viewModelScope.launch {
            messageRepo.createMessage(messageMap)
            shouldScrollToBottom = true
        }
    }

    private fun generateMessageMap(text: String, imageUri: Uri?): Map<String, Any> {
        val messageMap: MutableMap<String, Any> = mutableMapOf()
        messageMap["message"] = text

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
        val message = itemModel.data as? Message ?: return
        if (!message.isValid()) return
        val messageId = message.id
        val senderId = message.senderId
        _isLoading.value = true

        viewModelScope.launch {
            messageRepo.deleteMessage(messageId, senderId)
            _isLoading.value = false
        }
    }

    // Pin Message
    private fun pinMessage(itemModel: DynamicUIItem, isPinned: Boolean) {
        val message = itemModel.data as? Message ?: return
        if (!message.isValid()) return

        viewModelScope.launch {
            messageRepo.updateMessage(message.id, mapOf("is_pinned" to isPinned))
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
            val message = it.data as? Message ?: return@indexOfFirst false
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

        observeCurrentPageJob = viewModelScope.launch {
            messageRepo.listenMessages(conversationId, null, cursorCreatedAt)
                .collect {
                    updateCursorCreatedAt(it)
                }
        }
    }

    private fun getDateLong(index: Int): Long? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val message = item.data as? Message ?: return null
        if (!message.isValid()) return null
        val createdAt = message.createdAt ?: return null

        return DateHelper.convertRealmInstantToMillis(createdAt)
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

    fun updateSelectedSearchMessageIndex(messageId: String) {
        viewModelScope.launch {
            val index = _itemModels.value.indexOfFirst {
                it.itemTag == messageId
            }
            if (index >= 0) {
                _selectedSearchMessageIndex.value = index
            }
        }
    }
}