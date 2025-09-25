package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
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

interface ConversationRepository {
    fun getConversationFlow(conversationId: String): Flow<Conversation?>
    fun getConversationsFlow(): Flow<List<Conversation>>
}

class ConversationRepositoryImpl @Inject constructor() : ConversationRepository {

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
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ConversationModule {

    @Binds
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl): ConversationRepository
}