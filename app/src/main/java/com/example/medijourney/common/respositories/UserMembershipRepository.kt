package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserMembership
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserMembershipRepository {
    suspend fun observeUserMembership()
    fun getUserMembershipFow(): Flow<UserMembership?>
}

class UserMembershipRepositoryImpl @Inject constructor() : UserMembershipRepository   {
    override suspend fun observeUserMembership() {
        FireStoreManager.observeCollection(
            FireStoreCollection.USER_MEMBERSHIP,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }

    override fun getUserMembershipFow(): Flow<UserMembership?> {
        return RealmManager.flow(
            UserMembership::class,
            queryBuilder = RQueryBuilder()
                .equalTo(UserMembership::userId.name, FAManger.currentUserCode)
        ).map { it.firstOrNull() }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserMembershipModule {

    @Binds
    abstract fun bindUserMembershipRepository(
        impl: UserMembershipRepositoryImpl
    ): UserMembershipRepository
}