package com.example.medijourney.modules.chat.ai_chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.MessageRepo
import com.example.medijourney.common.respositories.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.Job
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
class AIChatViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val messageRepo: MessageRepo,
    messageRepository: MessageRepo
) : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    var shouldScrollToBottom = false
    private val conversationId: String = FAManger.currentUserCode.plus(Constants.CHAT_GPT)
    private var messagesFlow = messageRepository.geMessagesFLow(conversationId, null)
    private var user: User? = null
    private var cursorCreatedAt: Long? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            user = userRepo.getUserFlow(FAManger.currentUserCode).firstOrNull()
            observeLatestMessages()
            observeData()
        }
    }

    // Functions
    private suspend fun observeLatestMessages() {
        var firstHandled = false

        messageRepo
            .listenMessages(conversationId, null, null)
            .collect {
                if (firstHandled) {
                    updateCursorCreatedAt(it)
                    firstHandled = true
                }
            }
    }

    private suspend fun observeData() {

        messagesFlow
            .firstThenDebounce(500)
            .collectLatest {
                _itemModels.value = generateDynamicList(it)

            }
    }

    private fun generateDynamicList(messageResults: List<Message>): List<DynamicUIItem> {
        val size = messageResults.size
        val currentUserCode = FAManger.currentUserCode

        return messageResults.mapIndexedNotNull { index, entity ->
            if (!entity.isValid()) return@mapIndexedNotNull null
            var elderMessage: Message? = null
            val elderIndex = index + 1
            var laterMessage: Message? = null
            val laterIndex = index - 1
            val isComingMessage = currentUserCode != entity.senderId

            if (elderIndex < size) {
                elderMessage = messageResults[elderIndex]
            }
            if (laterIndex >= 0) {
                laterMessage = messageResults[laterIndex]
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
                    Constants.ENABLE_TYPED_ANIMATION to (index == 0 && isComingMessage)
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

    // Pagination
    fun fetchNextPage(index: Int) {
        val dateLong = getDateLong(index) ?: return
        val cursorCreatedAt = cursorCreatedAt ?: return
        if (dateLong >= cursorCreatedAt) return

        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        observeCurrentPageJob = viewModelScope.launch {
            messageRepo.listenMessages(conversationId, null, dateLong).collect {
                updateCursorCreatedAt(it)
            }
        }
    }

    private fun getDateLong(index: Int): Long? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val userMessage = item.data as? Message ?: return null
        if (!userMessage.isValid()) return null
        val createdAt = userMessage.createdAt ?: return null

        return DateHelper.convertRealmInstantToMillis(createdAt)
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