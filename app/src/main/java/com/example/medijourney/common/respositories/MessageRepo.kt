package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Message
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface MessageRepo {
    suspend fun createMessage(map: Map<String, Any>)
    suspend fun listenMessages(conversationId: String?, keyword: String?, cursorDate: Long?): Flow<List<Map<String, Any>>>
    fun getMessageFlow(messageId: String) : Flow<Message?>
    fun geMessagesFLow(conversationId: String?, keyword: String?) : Flow<List<Message>>
    fun getPinnedMessagesFlow(conversationId: String) : Flow<List<Message>>
    suspend fun updateMessage(messageId: String, map: Map<String, Any>): Boolean
    suspend fun deleteMessage(messageId: String, senderId: String): Boolean
}

class MessageRepoImpl @Inject constructor(): MessageRepo {
    override suspend fun createMessage(map: Map<String, Any>) {
        FireStoreManager.createDoc(FireStoreCollection.MESSAGES, data = map)
    }

    override suspend fun listenMessages(
        conversationId: String?,
        keyword: String?,
        cursorDate: Long?
    ): Flow<List<Map<String, Any>>> {
        return FireStoreManager.getDataFlow(
            FireStoreCollection.MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("member_ids.${FAManger.currentUserCode}", true)
                .apply {
                    if (conversationId != null) {
                        equalTo("conversation_id", conversationId)
                    }
                    if (!keyword.isNullOrEmpty()) {
                        arrayContains("keywords", keyword)
                    }
                    if (cursorDate != null) {
                        lessThan(Constants.CREATED_AT, cursorDate)
                        greaterThan(Constants.CREATED_AT, cursorDate)
                    }
                }
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        )
    }

    override fun getMessageFlow(messageId: String): Flow<Message?> {
        return RealmManager.flow(Message::class, messageId)
    }

    override fun geMessagesFLow(
        conversationId: String?,
        keyword: String?
    ): Flow<List<Message>> {
        return RealmManager.flow(
            Message::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (conversationId != null) {
                        equalTo(Message::conversationId.name, conversationId)
                    }
                    if (!keyword.isNullOrEmpty()) {
                        contains(Message::keywords.name, keyword)
                    }
                }
                .equalTo(Message::conversationId.name, conversationId)
                .sort(Message::createdAt.name, Sort.DESCENDING)
        )
    }

    override fun getPinnedMessagesFlow(conversationId: String): Flow<List<Message>> {
        return RealmManager.flow(
            Message::class,
            queryBuilder = RQueryBuilder()
                .equalTo(Message::conversationId.name, conversationId)
                .equalTo(Message::isPinned.name, true)
                .sort(Message::createdAt.name, Sort.DESCENDING)
        )
    }

    override suspend fun updateMessage(
        messageId: String,
        map: Map<String, Any>
    ): Boolean {
        return FireStoreManager.updateDoc(
            FireStoreCollection.MESSAGES,
            messageId,
            map
        )
    }

    override suspend fun deleteMessage(
        messageId: String,
        senderId: String
    ): Boolean {
        if (senderId == FAManger.currentUserCode) {
            return FireStoreManager.deleteDoc(FireStoreCollection.MESSAGES, messageId)
        } else {
            val data = FireStoreManager.getDoc(FireStoreCollection.MESSAGES, messageId).data
            val memberIds = (data?.get("member_ids") as? MutableMap<*, *>)?.toMutableMap()
            val currentUserCode = FAManger.currentUserCode

            return if (memberIds != null) {
                memberIds[currentUserCode] = true
                FireStoreManager.updateDoc(
                    FireStoreCollection.MESSAGES,
                    messageId,
                    mapOf("member_ids" to memberIds)
                )
            } else {
                false
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MessageModule {

    @Binds
    abstract fun bindMessageRepository(
    impl: MessageRepoImpl): MessageRepo
}
