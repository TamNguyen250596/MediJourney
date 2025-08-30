package com.example.medijourney.common.respository

import com.example.medijourney.common.extensions.inValues
import com.example.medijourney.common.extensions.toFlow
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Advertisement
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AdvertisementRepository {
    fun getAdvertisements(locations: List<String>): Flow<List<Advertisement>>
}

class AdvertisementImpl @Inject constructor() : AdvertisementRepository {
    override fun getAdvertisements(locations: List<String>): Flow<List<Advertisement>> {
        return RealmManager
            .query(Advertisement::class.java)
            .inValues(Advertisement::location.name, locations)
            .toFlow()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AdvertisementModule {

    @Binds
    abstract fun bindAdvertisementRepository(
        advertisementImpl: AdvertisementImpl
    ): AdvertisementRepository
}