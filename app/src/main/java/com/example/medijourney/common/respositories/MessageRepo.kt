package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
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
    suspend fun getMessages(conversationIds: List<String>, keyword: String?, cursorDate: Long? = null): List<MutableMap<String, Any>>
    suspend fun observePinnedMessages(conversationId: String)
    fun geMessagesFLow(conversationId: String) : Flow<List<Message>>
    fun geMessagesFLow(conversationIds: List<String>, keyword: String? = null) : Flow<List<Message>>
    fun getPinnedMessagesFlow(conversationId: String) : Flow<List<Message>>
}

class MessageRepoImpl @Inject constructor(): MessageRepo {
    override suspend fun getMessages(
        conversationIds: List<String>,
        keyword: String?,
        cursorDate: Long?
    ): List<MutableMap<String, Any>> {
        val snapshot = FireStoreManager.getCollection(
            FireStoreCollection.MESSAGES,
            queryBuilder = FSQueryBuilder()
                .inValues("conversation_id", conversationIds)
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        arrayContains("keywords", keyword)
                    }
                    if (cursorDate != null) {
                        greaterThan(Constants.CREATED_AT, cursorDate)
                    }
                }
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        )
        val list = snapshot.documents.mapNotNull { it.data as? MutableMap<String, Any> }
        RealmManager.write(Message(), list)
        return list
    }

    override suspend fun observePinnedMessages(conversationId: String) {
        FireStoreManager.observeCollection(
            FireStoreCollection.MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("conversation_id", conversationId)
                .equalTo("is_pinned", true)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
        )
    }

    override fun geMessagesFLow(conversationId: String): Flow<List<Message>> {
        return RealmManager.flow(
            Message::class,
            queryBuilder = RQueryBuilder()
                .equalTo(Message::conversationId.name, conversationId)
                .sort(Message::createdAt.name, Sort.DESCENDING)
        )
    }

    override fun geMessagesFLow(
        conversationIds: List<String>,
        keyword: String?
    ): Flow<List<Message>> {
        return RealmManager.flow(
            Message::class,
            queryBuilder = RQueryBuilder()
                .inValues(Message::conversationId.name, conversationIds)
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        contains(Message::message.name, keyword)
                    }
                }
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
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MessageModule {

    @Binds
    abstract fun bindMessageRepository(
    impl: MessageRepoImpl): MessageRepo
}
