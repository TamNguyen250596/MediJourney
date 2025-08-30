package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class User: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var avatarUrl: String? = null
    var backgroundUrl: String? = null
    var email: String = ""
    var displayName: String? = null
    var bio: String? = null
    var fullName: String? = null
    var gender: Long? = null
    var birthDate: RealmInstant? = null
    var mobileNo: String? = null
    var address: String? = null

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return User().apply {
            id = map["id"] as? String ?: ""
            avatarUrl = map["avatar_url"] as? String
            backgroundUrl = map["background_url"] as? String
            email = map["email"] as? String ?: ""
            displayName = map["display_name"] as? String
            bio = map["bio"] as? String
            fullName = map["full_name"] as? String
            gender = map["gender"] as? Long
            birthDate = map.getRealmInstant("birth_date")
            mobileNo = map["mobile_no"] as? String
            address = map["address"] as? String
        }
    }

    override fun update(map: Map<String, Any>) {
        avatarUrl = map["avatar_url"] as? String ?: avatarUrl
        backgroundUrl = map["background_url"] as? String ?: backgroundUrl
        email = map["email"] as? String ?: email
        displayName = map["display_name"] as? String ?: displayName
        bio = map["bio"] as? String ?: bio
        fullName = map["full_name"] as? String ?: fullName
        gender = map["gender"] as? Long ?: gender
        mobileNo = map["mobile_no"] as? String ?: mobileNo
        address = map["address"] as? String ?: address
        birthDate = map.getRealmInstant("birth_date", birthDate)
    }
}