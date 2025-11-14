package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Advertisement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AdvertisementRepo {
    fun getAdvertisements(locations: List<String>): Flow<List<Advertisement>>
}

class AdvertisementRepoImpl @Inject constructor() : AdvertisementRepo {
    override fun getAdvertisements(locations: List<String>): Flow<List<Advertisement>> {
        return RealmManager.flow(
                Advertisement::class,
                queryBuilder = RQueryBuilder()
                    .inValues(Advertisement::location.name, locations)
        )
    }
}