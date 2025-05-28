package com.example.medijourney.common.constants

import com.example.medijourney.R

enum class MessageMenuAction(val value: Int) {
    DELETE(R.string.delete),
    PIN(R.string.pin),
    UNPIN(R.string.unpin)
}

enum class EditProfileField {
    BACKGROUND,
    AVATAR,
    DISPLAY_NAME,
    BIO,
    FULL_NAME,
    GENDER,
    BIRTH_DATE,
    EMAIL,
    MOBILE_NUMBER,
    ADDRESS
}