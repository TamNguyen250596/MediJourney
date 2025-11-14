package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserMembership
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserMembershipRepo {
    suspend fun observeUserMembership()
    fun getUserMembershipFow(): Flow<UserMembership?>
    suspend fun updateMembership(id: String, data: Map<String, Any>): Boolean
}

class UserMembershipRepoImpl @Inject constructor() : UserMembershipRepo   {
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

    override suspend fun updateMembership(
        id: String,
        data: Map<String, Any>
    ): Boolean {
        return FireStoreManager.updateDoc(FireStoreCollection.USER_MEMBERSHIP, id, data)
    }
}