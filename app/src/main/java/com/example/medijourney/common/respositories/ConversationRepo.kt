package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Conversation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ConversationRepo {
    fun getConversationFlow(conversationId: String): Flow<Conversation?>
    fun getConversationsFlow(): Flow<List<Conversation>>
    fun getConversationsFlow(keyword: String?): Flow<List<Conversation>>
    fun observeConversations(keyword: String?, cursor: String?): Flow<List<MutableMap<String, Any>>>
}

class ConversationRepoImpl @Inject constructor() : ConversationRepo {

    override fun getConversationFlow(conversationId: String): Flow<Conversation?> {
        return RealmManager.flow(
                Conversation::class,
                queryBuilder = RQueryBuilder()
                    .equalTo(Conversation::id.name, conversationId)
        ).map { it.firstOrNull() }
    }

    override fun getConversationsFlow(): Flow<List<Conversation>> {
        return RealmManager.flow(
                Conversation::class,
                queryBuilder = RQueryBuilder()
                    .sort(Conversation::tag.name, Sort.ASCENDING)
            )
    }

    override fun getConversationsFlow(keyword: String?): Flow<List<Conversation>> {
        return RealmManager.flow(
            Conversation::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        contains(Conversation::keywords.name, keyword)
                    }
                }
                .equalTo(Conversation::isAdded.name, false)
                .sort(Conversation::tag.name, Sort.ASCENDING)
        )
    }

    override fun observeConversations(
        keyword: String?,
        cursor: String?
    ): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        equalTo("keyword", keyword)
                    }
                    if (cursor != null) {
                        greaterThan("tag", cursor)
                        lessThan("tag", cursor)
                    }
                }
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(Conversation(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ConversationModule {

    @Binds
    abstract fun bindConversationRepository(
        impl: ConversationRepoImpl): ConversationRepo
}