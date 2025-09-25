package com.example.medijourney.modules.chat.ai_chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.MessageRepository
import com.example.medijourney.common.respositories.UserMessageRepository
import com.example.medijourney.common.respositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.FlowPreview
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
class AIChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userMessageRepository: UserMessageRepository,
    messageRepository: MessageRepository
) : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    var shouldScrollToBottom = false
    private val conversationId: String = FAManger.currentUserCode.plus(Constants.CHAT_GPT)
    private val userMessagesFlow = userMessageRepository.getUserMessageFLow(conversationId)
    private var messagesFlow = messageRepository.geMessagesFLow(conversationId)
    private var user: User? = null
    private var cursorCreatedAt: Long? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            user = userRepository.getUserFlow(FAManger.currentUserCode).firstOrNull()
            observeLatestMessages()
            observeData()
        }
    }

    // Functions
    private suspend fun observeLatestMessages() {
        var firstHandled = false

        userMessageRepository
            .observeLatestMessages(conversationId)
            .collect {
                if (firstHandled) {
                    updateCursorCreatedAt(it)
                    firstHandled = true
                }
            }
    }

    @OptIn(FlowPreview::class)
    @Suppress("NAME_SHADOWING")
    private suspend fun observeData() {

        combine(
            messagesFlow,
            userMessagesFlow
        ) { _, userMessageResults ->
            userMessageResults
        }
            .firstThenDebounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicList(it)

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
                    Constants.ENABLE_TYPED_ANIMATION to (index == 0 && isComingMessage)
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
        val lastDoc = documents.lastOrNull() ?: return
        val eldestCreatedDateNumber = lastDoc[Constants.CREATED_AT] as? Long ?: return
        val eldestCreatedDateLong = eldestCreatedDateNumber
        val cursorDate = cursorCreatedAt
        if (cursorDate != null && eldestCreatedDateLong > cursorDate) return

        cursorCreatedAt = eldestCreatedDateLong
    }
}