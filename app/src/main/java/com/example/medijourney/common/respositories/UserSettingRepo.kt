package com.example.medijourney.common.respositories

import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserSettingRepo {
    fun getUserSettings(): Flow<List<UserSetting>>
}

class UserSettingRepoImpl @Inject constructor() : UserSettingRepo {
    override fun getUserSettings(): Flow<List<UserSetting>> {
        return RealmManager.flow(UserSetting::class)
    }
}