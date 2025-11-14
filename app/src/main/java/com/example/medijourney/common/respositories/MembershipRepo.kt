package com.example.medijourney.common.respositories

import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Membership
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface MembershipRepo {
    suspend fun observeMemberships()
    fun getMembershipsFlow(): Flow<List<Membership>>
}

class MembershipRepoImpl @Inject constructor() : MembershipRepo {
    override suspend fun observeMemberships() {
        FireStoreManager.observeCollection(FireStoreCollection.MEMBERSHIPS)
    }

    override fun getMembershipsFlow(): Flow<List<Membership>> {
        return RealmManager.flow(Membership::class)
    }
}